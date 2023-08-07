/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wpsPeasantFamily.Agent.Guards;

import BESA.Kernel.Agent.Event.DataBESA;

/**
 *
 * @author jairo
 */
public class ToControlMessage extends DataBESA {

    private String peasantFamilyAlias;
    private int days;
    private boolean peasantAlive;

    public ToControlMessage(String peasantFamilyAlias, int days) {
        this.setPeasantFamilyAlias(peasantFamilyAlias);
        this.setDays(days);
    }

    public ToControlMessage(String peasantFamilyAlias, int days, boolean alive) {
        this.setPeasantAlive(alive);
        this.setPeasantFamilyAlias(peasantFamilyAlias);
        this.setDays(days);
    }

    public void setPeasantAlive(boolean peasantAlive) {
        this.peasantAlive = peasantAlive;
    }

    public String getPeasantFamilyAlias() {
        return peasantFamilyAlias;
    }

    public void setPeasantFamilyAlias(String peasantFamilyAlias) {
        this.peasantFamilyAlias = peasantFamilyAlias;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

}
