
package model;

import java.sql.Timestamp;

public class IngredientAlert {
    private int alertId;
    private Ingredient ingredient;
    private AlertType type;
    private AlertStatus status;
    private String confirmedBy;
    private Timestamp createdAt;
    private Timestamp confirmedAt;

    public IngredientAlert() {
    }

    public IngredientAlert(int alertId, Ingredient ingredient, AlertType type, AlertStatus status, String confirmedBy, Timestamp createdAt, Timestamp confirmedAt) {
        this.alertId = alertId;
        this.ingredient = ingredient;
        this.type = type;
        this.status = status;
        this.confirmedBy = confirmedBy;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    
}
