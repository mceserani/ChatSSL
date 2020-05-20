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

package it.edu.polomanettiporciatti.chatssl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Matteo Ceserani
 * @version 1.0
 */

public class ChatSSL {

    // Posizione del keystore in cui è memorizzato il certificato utilizzato dal server
    private static final String KEYSTORE = "D:\\Documenti\\Scuola Statale\\Materiale didattico\\Web Services\\Progetti\\chatssl\\src\\main\\java\\it\\edu\\polomanettiporciatti\\chatssl\\SrvKeyStore";
    
    // Password per accedere al keystore
    private static final String STOREPASS = "P@ssw0rd!";
    
    /**
     * @param args the command line arguments. Type -h for help.
     */
    public static void main(String[] args) {
        
        SSLServerSocket server;
        SSLServerSocketFactory serverSocketFactory;
        SSLSocketFactory socketFactory;
        SSLSocket channel = null;
        ChatRicevitore ricevitore;
        ChatTrasmettitore trasmettitore;
        Future<?> future_ric;
        Future<?> future_tras;
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        
        BufferedReader tastiera = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            
            // Creazione dell'infrastruttura di rete del server
            if (args[0].equals("-s")) {
                
                // Numero di parametri non valido
                if (args.length != 2){
                    System.out.println("Invalid parameters. Use -h for help.");
                    return;
                }
                
                // Imposta le System Properties relative al keystore del server
                // per la JVM che esegue il server
                System.setProperty("javax.net.ssl.keyStore",ChatSSL.KEYSTORE);
                System.setProperty("javax.net.ssl.keyStorePassword",ChatSSL.STOREPASS);
                
                // Crea il Server Socket SSL
                // I cast sono necessari perchè i metodi:
                // - getDefault()
                // - createServerSocket()
                // - accept()
                // sono ereditati dalle superclassi ServerSocketFactory e ServerSocket
                // e quindi restituiscono un riferimento a oggetti della superclasse
                serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                server = (SSLServerSocket) serverSocketFactory.createServerSocket(Integer.parseInt(args[1]));
                System.out.println("Waiting for incoming connection...");
                channel = (SSLSocket) server.accept();
            }
            
            // Creazione dell'infrastruttura di rete del client
            else if (args[0].equals("-c")) {
                
                // Numero di parametri non valido
                if (args.length != 3){
                    System.out.println("Invalid parameters. Use -h for help.");
                    return;
                }
                
                // Crea il Socket SSL
                // I cast sono necessari perchè i metodi:
                // - getDefault()
                // - createSocket()
                // sono ereditati dalla superclasse SocketFactory
                // e quindi restituiscono un riferimento a oggetti della superclasse
                socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                channel = (SSLSocket) socketFactory.createSocket(args[1],Integer.parseInt(args[2]));
                channel.startHandshake();
               
            } 
            
            // Visualizza l'help
            else if (args[0].equals("-h")){
                
                // Numero di parametri non valido
                if (args.length != 1){
                    System.out.println("Invalid parameters. Use -h for help.");
                    return;
                }
                // Metodo statico che visualizza l'help
                ChatSSL.help();
            } 
            
            else {
                System.out.println("Invalid parameters. Use -h for help.");
                return;
            }
            
            ricevitore = new ChatRicevitore(channel);
            future_ric = executorService.submit(ricevitore);
            trasmettitore = new ChatTrasmettitore(channel,tastiera);
            future_tras = executorService.submit(trasmettitore);
        
        } catch (IOException | NullPointerException | NumberFormatException ex) {
            Logger.getLogger(ChatSSL.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        /**
         * Problema della terminazione dei thread impegnati in I/O bloccanti
         * Ipotizziamo che A(lice) invii a B(ob) il comando /exit
         * 1. Il thread trasmettitore di A termina regolarmente (future_tras che invia /exit)
         * 2. Il thread ricevitore di B termina regolarmente (future_ric che riceve /exit)
         * 3. Il thread ricevitore di A viene terminato chiudendo il socket
         * 4. Il thread trasmettitore di B viene terminato invitando l'utente a premere INVIO 
         *    per far sì che il trasmettitore stesso rilasci il monitor sulla tastiera 
         *    e subito dopo chiudendo lo stream tastiera (bug spacciato per feature)
         * Una volta terminati i task dei thread si procede allo shutdown dell'executorService
         */
        
        /**
         * Versione alternativa da sviluppare: CHANNELS
         */
        
        while(!future_ric.isDone()&&!future_tras.isDone());
        
        try {
            if (future_ric.isDone()){
                System.out.println("Il tuo interlocutore ha chiuso la chat. Premi INVIO per uscire.");
                tastiera.close();
                future_tras.cancel(true);
            } else {
                channel.close();
                future_ric.cancel(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatSSL.class.getName()).log(Level.SEVERE, null, ex);
        }

        executorService.shutdown();
        
        System.out.println("Bye!");
        
    }
    
    private static void help(){
        
        System.out.println("Usage: java -jar " + ChatSSL.class.getName() + ".jar [options]");
        System.out.println("Options for server operation: -s <port_number>");
        System.out.println("Options for client operation: -c <server IP or host name> <port_number>");
        System.out.println("Options for help: -h");
        
    }
    
}