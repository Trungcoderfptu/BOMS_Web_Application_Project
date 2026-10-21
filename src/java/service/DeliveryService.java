package service;

import dao.DeliveryDAO;
import dao.UserDAO;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DeliveryArea;
import model.DeliveryStatusHistory;
import model.ShipperProfile;
import model.User;
import utility.Constants;
import utility.ValidationException;

public class DeliveryService {

    private DeliveryDAO deliveryDAO;
    private UserDAO userDAO;

    public DeliveryService() {
        this.deliveryDAO = new DeliveryDAO();
        this.userDAO = new UserDAO();
    }

    public List<Map<String, String>> getTasksForShipper(String shipperUsername) throws ValidationException {
        List<Map<String, String>> resultList = new ArrayList<>();
        List<Map<String, Object>> tasks = deliveryDAO.getTasksByShipper(shipperUsername);
        for (Map<String, Object> t : tasks) {
            resultList.add(buildTaskMap(t));
        }
        return resultList;
    }

    public List<Map<String, String>> getAvailableTasksForShipper(String shipperUsername) throws ValidationException {
        List<Map<String, String>> resultList = new ArrayList<>();
        List<Map<String, Object>> tasks = deliveryDAO.getAvailableTasksForShipper(shipperUsername);
        for (Map<String, Object> t : tasks) {
            resultList.add(buildTaskMap(t));
        }
        return resultList;
    }

    public List<Map<String, String>> getAvailableShippers() throws ValidationException {
        List<Map<String, String>> resultList = new ArrayList<>();
        List<User> allUsers = userDAO.getAllUsers(); 

        if (allUsers != null) {
            for (User u : allUsers) {
                if (Constants.ROLE_SHIPPER.equalsIgnoreCase(u.getRole())) {
                    Map<String, String> statusInfo = buildShipperStatusMap(u.getUsername());
                    resultList.add(buildShipperMap(u, statusInfo));
                }
            }
        }
        return resultList;
    }

    public List<Map<String, String>> getUnassignedTasks() throws ValidationException {
        List<Map<String, String>> unassignedList = new ArrayList<>();
        List<Map<String, Object>> allTasks = deliveryDAO.getAllTasks();
        for (Map<String, Object> t : allTasks) {
            if (isTaskWaitingForShipper(t)) {
                unassignedList.add(buildTaskMap(t));
            }
        }
        return unassignedList;
    }

    public void assignTaskToShipper(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        Map<String, Object> task = getAndValidateTask(taskId);
        
        validateTaskIsFree(task);
        validateShipperCapacity(shipperUsername); 
        
        deliveryDAO.updateTaskShipper(taskId, shipperUsername); 
        deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_WAITING_DELIVERY);

        int shipperId = deliveryDAO.getUserIdByUsername(shipperUsername);
        if (shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, taskId, shipperId, Constants.STATUS_WAITING_DELIVERY,
                    Constants.NOTE_DISPATCH_ASSIGN, null, new Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);
        }
    }

    public void updateShipperTaskStatus(String taskIdStr, String shipperUsername, String newStatus) throws ValidationException {
        updateShipperTaskStatus(taskIdStr, shipperUsername, newStatus, null, null);
    }

    public void updateShipperTaskStatus(String taskIdStr, String shipperUsername, String newStatus, String failReason, String proofImage) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        Map<String, Object> task = getAndValidateTask(taskId);

        validateShipperAuthorization(shipperUsername, (String) task.get("shipperUsername"));
        validateStatusFlow((String) task.get("status"));

        if (Constants.STATUS_FAILED.equals(newStatus)) {
            if (failReason == null || failReason.trim().isEmpty()) {
                throw new ValidationException(Constants.ERROR_EMPTY_FAIL_REASON);
            }
        }

        deliveryDAO.updateTaskStatus(taskId, newStatus);

        int shipperId = deliveryDAO.getUserIdByUsername(shipperUsername);
        if (shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, taskId, shipperId, newStatus,
                    (failReason != null && !failReason.trim().isEmpty()) ? failReason.trim() : null,
                    proofImage, new Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);
            syncShipperProfileStatus(shipperId, shipperUsername);
        }
    }

    public void claimTask(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        Map<String, Object> task = getAndValidateTask(taskId);

        validateTaskIsFree(task);
        validateShipperCapacity(shipperUsername);

        deliveryDAO.claimTask(taskId, shipperUsername);

        int shipperId = deliveryDAO.getUserIdByUsername(shipperUsername);
        if (shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, taskId, shipperId, Constants.STATUS_DELIVERING,
                    Constants.NOTE_SHIPPER_CLAIM, null, new Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);
            syncShipperProfileStatus(shipperId, shipperUsername);
        }
    }

    public void acceptTask(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        Map<String, Object> task = getAndValidateTask(taskId);

        validateShipperAuthorization(shipperUsername, (String) task.get("shipperUsername"));
        validateTaskIsPending(task);
        validateShipperCapacity(shipperUsername);

        deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_DELIVERING);

        int shipperId = deliveryDAO.getUserIdByUsername(shipperUsername);
        if (shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, taskId, shipperId, Constants.STATUS_DELIVERING,
                    Constants.NOTE_SHIPPER_ACCEPT, null, new Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);
            syncShipperProfileStatus(shipperId, shipperUsername);
        }
    }

    public void rejectTask(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        Map<String, Object> task = getAndValidateTask(taskId);

        validateShipperAuthorization(shipperUsername, (String) task.get("shipperUsername"));
        validateTaskIsPending(task); 

        int shipperId = deliveryDAO.getUserIdByUsername(shipperUsername);
        if (shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, taskId, shipperId, Constants.STATUS_WAITING_DISPATCH,
                    Constants.NOTE_SHIPPER_REJECT, null, new Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);
            syncShipperProfileStatus(shipperId, shipperUsername);
        }

        deliveryDAO.updateTaskShipper(taskId, null); 
        deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_WAITING_DISPATCH);
    }

    private Map<String, String> buildTaskMap(Map<String, Object> task) {
        Map<String, String> map = new HashMap<>();
        map.put("taskId", String.valueOf(task.get("taskId")));
        map.put("orderCode", (String) task.get("orderCode"));
        map.put("customerAddress", task.get("customerAddress") != null ? (String) task.get("customerAddress") : "");
        map.put("status", task.get("status") != null ? (String) task.get("status") : "");
        map.put("customerName", task.get("customerName") != null ? (String) task.get("customerName") : "");
        map.put("customerPhone", task.get("customerPhone") != null ? (String) task.get("customerPhone") : "");
        map.put("areaName", task.get("areaName") != null ? (String) task.get("areaName") : "");
        double total = (task.get("totalAmount") instanceof Number) ? ((Number) task.get("totalAmount")).doubleValue() : 0.0;
        map.put("totalAmount", String.format("%,.0f", total));
        map.put("shipperUsername", task.get("shipperUsername") != null ? (String) task.get("shipperUsername") : "");
        return map;
    }

    private Map<String, String> buildShipperStatusMap(String username) throws ValidationException {
        int activeOrders = countActiveOrders(username); 
        ShipperProfile profile = deliveryDAO.getShipperProfileByUsername(username);
        int maxOrders = (profile != null && profile.getMaxOrders() > 0) ? profile.getMaxOrders() : Constants.DEFAULT_SHIPPER_MAX_ORDERS;

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("workingStatus", (activeOrders == 0) ? Constants.SHIPPER_WORK_READY : Constants.SHIPPER_WORK_DELIVERING);
        statusMap.put("activeOrders", String.valueOf(activeOrders));
        statusMap.put("maxOrders", String.valueOf(maxOrders));
        return statusMap;
    }
    
    private Map<String, String> buildShipperMap(User u, Map<String, String> statusInfo) {
        Map<String, String> map = new HashMap<>();
        map.put("username", u.getUsername());
        map.put("fullName", u.getFullName());
        map.put("address", u.getAddress());
        map.put("phone", u.getPrimaryPhone());
        map.putAll(statusInfo); 
        return map;
    }

    private int countActiveOrders(String username) throws ValidationException {
        int count = 0;
        List<Map<String, Object>> tasks = deliveryDAO.getTasksByShipper(username);
        for (Map<String, Object> t : tasks) {
            if (Constants.STATUS_DELIVERING.equals(t.get("status"))) {
                count++;
            }
        }
        return count;
    }

    private boolean isTaskWaitingForShipper(Map<String, Object> t) {
        String status = (String) t.get("status");
        String shipper = (String) t.get("shipperUsername");
        boolean isStatusWaiting = Constants.STATUS_WAITING_DISPATCH.equals(status)
                || Constants.STATUS_WAITING_DELIVERY.equals(status);
        boolean isShipperEmpty = (shipper == null || shipper.trim().isEmpty());
        return isStatusWaiting && isShipperEmpty;
    }

    private int parseTaskId(String taskIdStr) throws ValidationException {
        try {
            return Integer.parseInt(taskIdStr);
        } catch (NumberFormatException e) {
            throw new ValidationException(Constants.ERROR_INVALID_TASK);
        }
    }

    private Map<String, Object> getAndValidateTask(int taskId) throws ValidationException {
        Map<String, Object> task = deliveryDAO.getTaskById(taskId);
        if (task == null) {
            throw new ValidationException(Constants.ERROR_INVALID_TASK);
        }
        return task;
    }

    private void validateShipperAuthorization(String loggedInShipper, String assignedShipper) throws ValidationException {
        if (!loggedInShipper.equals(assignedShipper)) {
            throw new ValidationException(Constants.ERROR_UNAUTHORIZED_SHIPPER);
        }
    }

    private void validateStatusFlow(String currentStatus) throws ValidationException {
        if (Constants.STATUS_COMPLETED.equals(currentStatus) || Constants.STATUS_FAILED.equals(currentStatus)) {
            throw new ValidationException(Constants.ERROR_INVALID_STATUS_FLOW);
        }
    }

    private void validateTaskIsFree(Map<String, Object> task) throws ValidationException {
        String shipper = (String) task.get("shipperUsername");
        if (shipper != null && !shipper.trim().isEmpty()) {
            throw new ValidationException(Constants.ERROR_TASK_ALREADY_ASSIGNED); 
        }
    }

    private void validateShipperCapacity(String shipperUsername) throws ValidationException {
        ShipperProfile profile = deliveryDAO.getShipperProfileByUsername(shipperUsername);
        int maxAllowed = (profile != null && profile.getMaxOrders() > 0) ? profile.getMaxOrders() : Constants.DEFAULT_SHIPPER_MAX_ORDERS;
        int activeCount = countActiveOrders(shipperUsername);
        if (activeCount >= maxAllowed) { 
            throw new ValidationException(Constants.ERROR_SHIPPER_FULL); 
        }
    }

    private void validateTaskIsPending(Map<String, Object> task) throws ValidationException {
        String status = (String) task.get("status");
        if (!Constants.STATUS_WAITING_DELIVERY.equals(status)
                && !Constants.STATUS_PENDING.equals(status)
                && !Constants.STATUS_WAITING_DISPATCH.equals(status)) {
            throw new ValidationException(Constants.ERROR_CANNOT_REJECT); 
        }
    }

    private void syncShipperProfileStatus(int shipperId, String username) throws ValidationException {
        int activeCount = countActiveOrders(username);
        String status = (activeCount == 0) ? Constants.SHIPPER_STATUS_READY : Constants.SHIPPER_STATUS_DELIVERING;
        deliveryDAO.updateShipperStatusAndCount(shipperId, status, activeCount);
    }

    public List<Map<String, String>> getAllDeliveryAreasForView() throws ValidationException {
        List<Map<String, String>> list = new ArrayList<>();
        List<DeliveryArea> areas = deliveryDAO.getAllDeliveryAreas();
        for (DeliveryArea a : areas) {
            Map<String, String> m = new HashMap<>();
            m.put("areaId", String.valueOf(a.getAreaId()));
            m.put("areaName", a.getAreaName());
            list.add(m);
        }
        return list;
    }
}