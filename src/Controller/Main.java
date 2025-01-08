package Controller;

import View.LoginView;

/**
 * Main class for launching the server and clients, and displaying active threads
 */
public class Main {
    public static void main(String[] args) {
        //initialize server and clients
        Server server = new Server();

        //initialize login views for each client
        LoginView loginView1 = new LoginView(new ViewController());
        LoginView loginView2 = new LoginView(new ViewController());
        LoginView loginView3 = new LoginView(new ViewController());

    }
}
