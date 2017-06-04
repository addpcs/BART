/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.addpcs.util;

/**
 *
 * @author justin
 */
public class MutableBoolean {
    boolean val;

    public MutableBoolean(boolean b) {
        val = b;
    }

    public void setValue(boolean b) {
        val = b;
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public boolean equals(Object other) {
        if(other.getClass().equals(MutableBoolean.class)) {
            return this.hashCode() == ((MutableBoolean)other).hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.val ? 1 : 0);
        return hash;
    }
}
