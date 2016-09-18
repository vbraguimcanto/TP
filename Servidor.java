/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogo;

import java.net.*;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 *
 * @author victor
 */
public class Servidor {

    static ServerSocket serverSocket;
    static Socket player1, player2;
    static DataInputStream din;
    static DataOutputStream dout;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Aguardando Jogador(es) 2/2");
            player1 = serverSocket.accept();
            System.out.println("Aguardando Jogador(es) 1/2");
            player2 = serverSocket.accept();
            //din = new DataInputStream(socket.getInputStream());
            //dout = new DataOutputStream(socket.getOutputStream());
            
        } catch (Exception e) {

        }

    }
}
