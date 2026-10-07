/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.Date;

/**
 *
 * @author AD
 */
public class AccountManager {

    private int keyId;
    private String securityKey;
    private String role;
    private Integer userId;
    private boolean isActive;
    private Date createdAt;

    public AccountManager() {
    }

    public AccountManager(int keyId, String securityKey, String role, Integer userId, boolean isActive, Date createdAt) {
        this.keyId = keyId;
        this.securityKey = securityKey;
        this.role = role;
        this.userId = userId;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean getActive() {
        return isActive;
    }

}
