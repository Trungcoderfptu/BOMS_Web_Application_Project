package service;

import dao.DeliveryDAO;
import dao.UserDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DeliveryTask;
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
        List<DeliveryTask> tasks = deliveryDAO.getTasksByShipper(shipperUsername);
        for (DeliveryTask t : tasks) {
            resultList.add(buildTaskMap(t));
        }
        return resultList;
    }

    public List<Map<String, String>> getAvailableShippers() throws ValidationException {
        List<Map<String, String>> resultList = new ArrayList<>();
        List<User> allUsers = userDAO.getAllUsers(); 

        if (allUsers != null) {
            for (User u : allUsers) {
                if ("Shipper".equalsIgnoreCase(u.getRole())) {
                    Map<String, String> statusInfo = buildShipperStatusMap(u.getUsername());
                    resultList.add(buildShipperMap(u, statusInfo));
                }
            }
        }
        return resultList;
    }

    public List<Map<String, String>> getUnassignedTasks() throws ValidationException {
        List<Map<String, String>> unassignedList = new ArrayList<>();
        List<DeliveryTask> allTasks = deliveryDAO.getAllTasks();
        for (DeliveryTask t : allTasks) {
            if (isTaskWaitingForShipper(t)) {
                unassignedList.add(buildTaskMap(t));
            }
        }
        return unassignedList;
    }

    public void assignTaskToShipper(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        DeliveryTask task = getAndValidateTask(taskId);
        
        validateTaskIsFree(task);
        validateShipperCapacity(shipperUsername); 
        
        deliveryDAO.updateTaskShipper(taskId, shipperUsername); 
        
        if (Constants.STATUS_REJECTED.equals(task.getStatus())) {
            deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_PENDING);
        }
    }

    public void updateShipperTaskStatus(String taskIdStr, String shipperUsername, String newStatus) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        DeliveryTask task = getAndValidateTask(taskId);

        validateShipperAuthorization(shipperUsername, task.getShipperUsername());
        validateStatusFlow(task.getStatus());

        deliveryDAO.updateTaskStatus(taskId, newStatus);
    }

    public void rejectTask(String taskIdStr, String shipperUsername) throws ValidationException {
        int taskId = parseTaskId(taskIdStr);
        DeliveryTask task = getAndValidateTask(taskId);

        validateShipperAuthorization(shipperUsername, task.getShipperUsername());
        validateTaskIsPending(task); 

        deliveryDAO.updateTaskShipper(taskId, null); 
        deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_REJECTED);
    }

    private Map<String, String> buildTaskMap(DeliveryTask task) {
        Map<String, String> map = new HashMap<>();
        map.put("taskId", String.valueOf(task.getTaskId()));
        map.put("orderCode", task.getOrderCode());
        map.put("customerAddress", task.getCustomerAddress());
        map.put("status", task.getStatus());
        return map;
    }

    private Map<String, String> buildShipperStatusMap(String username) throws ValidationException {
        int activeOrders = countActiveOrders(username); 

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("workingStatus", (activeOrders == 0) ? "READY" : "DELIVERING");
        statusMap.put("activeOrders", String.valueOf(activeOrders));
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
        List<DeliveryTask> tasks = deliveryDAO.getTasksByShipper(username);
        for (DeliveryTask t : tasks) {
            if (Constants.STATUS_DELIVERING.equals(t.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean isTaskWaitingForShipper(DeliveryTask t) {
        boolean isStatusWaiting = Constants.STATUS_PENDING.equals(t.getStatus()) || Constants.STATUS_REJECTED.equals(t.getStatus());
        boolean isShipperEmpty = (t.getShipperUsername() == null || t.getShipperUsername().trim().isEmpty());
        return isStatusWaiting && isShipperEmpty;
    }

    private int parseTaskId(String taskIdStr) throws ValidationException {
        try {
            return Integer.parseInt(taskIdStr);
        } catch (NumberFormatException e) {
            throw new ValidationException(Constants.ERROR_INVALID_TASK);
        }
    }

    private DeliveryTask getAndValidateTask(int taskId) throws ValidationException {
        DeliveryTask task = deliveryDAO.getTaskById(taskId);
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

    private void validateTaskIsFree(DeliveryTask task) throws ValidationException {
        if (task.getShipperUsername() != null && !task.getShipperUsername().trim().isEmpty()) {
            throw new ValidationException(Constants.ERROR_TASK_ALREADY_ASSIGNED); 
        }
    }

    private void validateShipperCapacity(String shipperUsername) throws ValidationException {
        int activeCount = countActiveOrders(shipperUsername);
        if (activeCount >= 5) { 
            throw new ValidationException(Constants.ERROR_SHIPPER_FULL); 
        }
    }

    private void validateTaskIsPending(DeliveryTask task) throws ValidationException {
        if (!Constants.STATUS_PENDING.equals(task.getStatus())) {
            throw new ValidationException(Constants.ERROR_CANNOT_REJECT); 
        }
    }
}