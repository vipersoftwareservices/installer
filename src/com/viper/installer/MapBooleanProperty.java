/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java    1.00 2014/06/15
 *
 * Copyright 1998-2014 by Viper Software Services
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Viper Software Services. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Viper Software Services.
 *
 * @author Tom Nevin (TomNevin@pacbell.net)
 *
 * @version 1.0, 06/15/2014 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer;

import java.util.Map;

import javafx.beans.property.Property;
import javafx.beans.property.BooleanPropertyBase;

/**
 * This class provides a full implementation of a {@link Property} wrapping a {@code Boolean} value.
 * 
 * @see BooleanPropertyBase
 * 
 */
public class MapBooleanProperty extends BooleanPropertyBase {

    private final Map<String, Object> bean;
    private final String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBean() {
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * The constructor of {@code BooleanProperty}
     * 
     * @param bean
     *            the bean of this {@code BooleanProperty}
     * @param name
     *            the name of this {@code BooleanProperty}
     */
    public MapBooleanProperty(Object bean, String name) {
        this.bean = (Map<String, Object>) bean;
        this.name = name;
    }

    /**
     * The constructor of {@code BooleanProperty}
     * 
     * @param bean
     *            the bean of this {@code BooleanProperty}
     * @param name
     *            the name of this {@code BooleanProperty}
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public MapBooleanProperty(Object bean, String name, Boolean initialValue) {
        super(initialValue);
        this.bean = (Map<String, Object>) bean;
        this.name = name;
    }

    @Override
    public boolean get() {
        if (bean.get(name) == null) {
            return false;
        }
        return (Boolean) bean.get(name);
    }

    @Override
    public void set(boolean value) {
        bean.put(name, value);
    }
}
