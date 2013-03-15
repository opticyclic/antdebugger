package com.handyedit.ant.listener;

import com.handyedit.ant.listener.cmd.DebuggerCommandFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class DebuggerCommandListener {

    private DebuggerCommandFactory myFactory;
    private BufferedReader myReader;
    private BufferedWriter myWriter;

    public DebuggerCommandListener(BreakpointManager manager, BufferedReader reader, BufferedWriter writer) {
        myFactory = new DebuggerCommandFactory(manager);
        myReader = reader;
        myWriter = writer;
    }

    public void run() throws IOException {

        DebuggerCommand c;
        while ((c = read()) != null) {
            c.execute(myWriter);
        }
    }

    private DebuggerCommand read() throws IOException {
        String line = myReader.readLine();
        String[] cmd = line.split(",");
        return myFactory.create(cmd);
    }

    private DebuggerCommand readBreakpoint() throws IOException {
        String line = myReader.readLine();
        String[] cmd = line.split(",");
        if (myFactory.isBreakpointCommand(cmd)) {
            if (cmd.length < 3) {
                throw new RuntimeException("readBreakpoint(): " + line);
            }

            return myFactory.createBreakpointCommand(cmd);
        } else {
            return null;
        }
    }

     // reads sequence of breakpoint commands terminated by other command
    private void readBreakpoints() throws IOException {
        DebuggerCommand c;
        while ((c = readBreakpoint()) != null) {
            c.execute(myWriter);
        }
    }

    public static DebuggerCommandListener start(BreakpointManager manager, int port) throws IOException {
        ServerSocket listenSocket = new ServerSocket(port);
        listenSocket.setSoTimeout(60000);
        Socket s = listenSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        final DebuggerCommandListener listener = new DebuggerCommandListener(manager, in, out);
        listener.readBreakpoints();

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    listener.run();
                } catch (IOException e) {
                }
            }
        });

        t.start();

        return listener;
    }

    public void sendCommand(String... args) throws IOException {
        String s = join(args);
        myWriter.write(s);
        myWriter.newLine();
        myWriter.flush();
    }

    private String join(String... arr) {
        boolean first = true;
        StringBuffer result = new StringBuffer();
        for (String s : arr) {
            if (!first) {
                result.append(",");
            }
            first =  false;
            result.append(s);
        }
        return result.toString();
    }

    public void close() {
/*
        try {
            if (myReader != null) {
                myReader.close();
                myReader = null;
            }
        } catch (IOException e) {
        }
        try {
            if (myWriter != null) {
                myWriter.close();
                myWriter = null;
            }
        } catch (IOException e) {
        }
*/
        // todo: stop thread
    }
}
