/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import controller.Converter;
import controller.Requester;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael
 */
public class Teste implements Requester{
    
    public void start(){
        
        
        Converter c = new Converter(this);
        try {
            //c.conectarDBF("PERDIOESTE", "", "");
            c.conectarDBF("TESTEDBF", "", "");
            //c.conectarDest("RAFAEL-PC\\SAGE", "ferri0001", "SA", "Cordilheir@2008");
            c.conectarDest("localhost", "rafael", "root", "root");
            c.convert();
            c.desconectarDest();
            c.desconectarDBF();
            
        } catch (Exception ex) {
            Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        new Teste().start();
    }

    @Override
    public void addMessage(String msg) {
        System.out.println(msg);
    }
}
