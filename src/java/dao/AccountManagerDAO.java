package dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.AccountManager;

public class AccountManagerDAO {

    private static List<AccountManager> keyList = new ArrayList<>();

    static {
        keyList.add(new AccountManager(1, "BOMS-ADMIN", "Admin", 1, true, new Date()));
        keyList.add(new AccountManager(2, "BOMS-MGR", "Manager", 2, true, new Date()));
        keyList.add(new AccountManager(3, "BOMS2026", "Staff", null, true, new Date()));
        keyList.add(new AccountManager(4, "BOMS-SHIP", "Shipper", null, true, new Date()));
    }

    public List<AccountManager> getAllKeys() {
        return keyList;
    }

    public void insertKey(AccountManager am) {
        int newId = keyList.isEmpty() ? 1 : keyList.get(keyList.size() - 1).getKeyId() + 1;
        am.setKeyId(newId);
        keyList.add(am);
    }
}
