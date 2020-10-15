/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.material;

/**
 *
 * @author fmauz
 */
public interface MessagePresenter {

    public void error(String message, Object... args);

    public void info(String message, Object... args);

    public String presentMessage(String messageKey, Object... args);
}
