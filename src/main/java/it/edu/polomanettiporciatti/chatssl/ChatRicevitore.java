/* 	
	ChatSSL
    Copyright (C) 2020  PoloTecnologico

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

	Mail to: matteo.ceserani@polomanettiporciatti.edu.it
*/

package it.edu.polomanettiporciatti.ChatSSL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Matteo Ceserani
 * @version 1.0.0
 */
public class ChatRicevitore implements Runnable {
    
    private Socket inChannel;
    private BufferedReader buffer;
    
    public ChatRicevitore(Socket s) throws IOException, IllegalArgumentException{
        if(s.isConnected()){
            inChannel = s;
            try {
                buffer = new BufferedReader(new InputStreamReader(inChannel.getInputStream()));
                System.out.println("Ricevitore creato.");
            } catch (IOException ex){
                throw new IOException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void run(){
        
        @SuppressWarnings("UnusedAssignment")
        String message = "";
        
        do {
            try {
                message = buffer.readLine();
                if (!message.equals("/exit")){
                    System.out.print(inChannel.getInetAddress().toString() + " scrive: ");
                    System.out.println(message);
                }
            } catch (IOException ex) {
                return;
            }
        }while(!message.equals("/exit"));
        
    }
    
}