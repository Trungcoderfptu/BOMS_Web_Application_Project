package utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý bộ nhớ đệm thời gian thực cho đơn hàng (trong package Utility theo đúng Rule_BOMS)
 * Lưu trữ mốc thời gian hủy đơn để phục vụ cơ chế khóa hoàn tác sau 5 phút mà không cần can thiệp CSDL.
 */
public class OrderRuntimeCache {

    private static final Map<Integer, Long> CANCEL_TIMESTAMPS = new ConcurrentHashMap<>();

    private OrderRuntimeCache() {
    }

    public static void recordCancelTime(int orderId) {
        CANCEL_TIMESTAMPS.put(orderId, System.currentTimeMillis());
    }

    public static void clearCancelTime(int orderId) {
        CANCEL_TIMESTAMPS.remove(orderId);
    }

    public static Long getCancelTime(int orderId) {
        return CANCEL_TIMESTAMPS.get(orderId);
    }
}
