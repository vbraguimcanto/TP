package jogo;

import java.awt.Color;
import static java.awt.Color.BLACK;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author victor
 */
public class Cliente implements ActionListener, KeyListener, Runnable {

    static Socket socket;
    static DataInputStream dis;
    static DataOutputStream dos;
    public static Cliente snake;
    public JFrame jframe;
    public Painter renderPanel;
    public Timer timer = new Timer(20, this);
    public ArrayList<Point> snakeParts = new ArrayList<Point>();
    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, SCALE = 10;
    public int ticks = 0, direcaoMovimento = DOWN, pontosObtidos, tamanhoCobrinha = 10, tempo;
    public Point cabecaCobrinha, fruta;
    public Random random;
    public boolean over = false, jogoPausado;
    public Dimension dim;
    PrintWriter escritor;

    private Thread thread;
    private boolean accepted = false;
    private boolean unableToCommunicateWithOpponent = false;
    private boolean won = false;
    private boolean enemyWon = false;

    private int errors = 0;

    private Font font = new Font("Verdana", Font.BOLD, 32);
    private Font smallerFont = new Font("Verdana", Font.BOLD, 20);
    private Font largerFont = new Font("Verdana", Font.BOLD, 50);
    
    private String waitingString = "Esperando pelo outro jogador!";
    private String unableToCommunicateWithOpponentString = "Não foi possível realizar a comunicação com o oponente.";
    private String wonString = "Você venceu!";
    private String enemyWonString = "Você perdeu!";

    private ServerSocket serverSocket;

    private Painter painter;

    public Cliente() {

        painter = new Painter();
        painter.setPreferredSize(new Dimension(805, 700));

        dim = Toolkit.getDefaultToolkit().getScreenSize();
        jframe = new JFrame("Jogo da Cobrinha");
        jframe.setVisible(true);
        jframe.setSize(805, 700);
        jframe.setResizable(false);
        jframe.setLocation(dim.width / 2 - jframe.getWidth() / 2, dim.height / 2 - jframe.getHeight() / 2);
        jframe.add(renderPanel = new Painter());
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.addKeyListener(this);
        startGame();
    }

    public void startGame() {
        over = false;
        jogoPausado = false;
        tempo = 0;
        pontosObtidos = 0;
        tamanhoCobrinha = 14;
        ticks = 0;
        direcaoMovimento = DOWN;
        cabecaCobrinha = new Point(0, -1);
        random = new Random();
        snakeParts.clear();
        fruta = new Point(random.nextInt(79), random.nextInt(66));
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        renderPanel.repaint();
        ticks++;
        if (ticks % 2 == 0 && cabecaCobrinha != null && !over && !jogoPausado) {
            tempo++;
            snakeParts.add(new Point(cabecaCobrinha.x, cabecaCobrinha.y));
            if (direcaoMovimento == UP) {
                if (cabecaCobrinha.y - 1 >= 0 && noTailAt(cabecaCobrinha.x, cabecaCobrinha.y - 1)) {
                    cabecaCobrinha = new Point(cabecaCobrinha.x, cabecaCobrinha.y - 1);
                } else {
                    over = true;
                }
            }

            if (direcaoMovimento == DOWN) {
                if (cabecaCobrinha.y + 1 < 67 && noTailAt(cabecaCobrinha.x, cabecaCobrinha.y + 1)) {
                    cabecaCobrinha = new Point(cabecaCobrinha.x, cabecaCobrinha.y + 1);
                } else {
                    over = true;
                }
            }
            if (direcaoMovimento == LEFT) {
                if (cabecaCobrinha.x - 1 >= 0 && noTailAt(cabecaCobrinha.x - 1, cabecaCobrinha.y)) {
                    cabecaCobrinha = new Point(cabecaCobrinha.x - 1, cabecaCobrinha.y);
                } else {
                    over = true;
                }
            }
            if (direcaoMovimento == RIGHT) {
                if (cabecaCobrinha.x + 1 < 80 && noTailAt(cabecaCobrinha.x + 1, cabecaCobrinha.y)) {
                    cabecaCobrinha = new Point(cabecaCobrinha.x + 1, cabecaCobrinha.y);
                } else {
                    over = true;
                }
            }
            if (snakeParts.size() > tamanhoCobrinha) {
                snakeParts.remove(0);
            }
            if (fruta != null) {
                if (cabecaCobrinha.equals(fruta)) {
                    pontosObtidos += 10;
                    tamanhoCobrinha++;
                    fruta.setLocation(random.nextInt(79), random.nextInt(66));
                }
            }
        }
    }

    public boolean noTailAt(int x, int y) {
        for (Point point : snakeParts) {
            if (point.equals(new Point(x, y))) {
                return false;
            }
        }
        return true;
    }

    private void listenForServerRequest() {
        socket = null;
        try {
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
            System.out.println("Requisição do Cliente Aceita pelo Servidor!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean connect() {
        try {
            socket = new Socket("127.0.0.1", 8080);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
            return true;
        } catch (IOException e) {
            System.out.println("Não foi possível conectar ao endereço!");
            return false;
        }
    }

    public static void main(String[] args) {
        snake = new Cliente();
        snake.connect();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int i = e.getKeyCode();
        if ((i == KeyEvent.VK_A || i == KeyEvent.VK_LEFT) && direcaoMovimento != RIGHT) {
            direcaoMovimento = LEFT;
        }
        if ((i == KeyEvent.VK_D || i == KeyEvent.VK_RIGHT) && direcaoMovimento != LEFT) {
            direcaoMovimento = RIGHT;
        }
        if ((i == KeyEvent.VK_W || i == KeyEvent.VK_UP) && direcaoMovimento != DOWN) {
            direcaoMovimento = UP;
        }
        if ((i == KeyEvent.VK_S || i == KeyEvent.VK_DOWN) && direcaoMovimento != UP) {
            direcaoMovimento = DOWN;
        }
        if (i == KeyEvent.VK_SPACE) {
            if (over) {
                startGame();
            } else {
                jogoPausado = !jogoPausado;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void run() {
        while (true) {
            //tick();
            painter.repaint();
            if (!accepted) {
                listenForServerRequest();
            }
        }
    }

    public void tick() {
        if (errors >= 10) {
            unableToCommunicateWithOpponent = true;
        }
        if (!unableToCommunicateWithOpponent) {
            try {
                int space = dis.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void render(Graphics g) {
        //g.drawImage(board, 0, 0, null);
        if (unableToCommunicateWithOpponent) {
            g.setColor(Color.RED);
            g.setFont(smallerFont);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(unableToCommunicateWithOpponentString);
            g.drawString(unableToCommunicateWithOpponentString, 805 / 2 - stringWidth / 2, 700 / 2);
            return;
        }
    }

    public class Painter extends JPanel implements Runnable {

        public final Color GREEN = new Color(1666073);

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            render(g);
            Cliente snake = Cliente.snake;
            g.setColor(BLACK);
            g.fillRect(0, 0, 1000, 800);
            g.setColor(Color.GREEN);
            for (Point point : snake.snakeParts) {
                g.fillRect(point.x * Cliente.SCALE, point.y * Cliente.SCALE, Cliente.SCALE, Cliente.SCALE);
            }
            g.fillRect(snake.cabecaCobrinha.x * Cliente.SCALE, snake.cabecaCobrinha.y * Cliente.SCALE, Cliente.SCALE, Cliente.SCALE);
            // cria retângulo cobrinha
            g.setColor(Color.RED);
            g.fillRect(snake.fruta.x * Cliente.SCALE, snake.fruta.y * Cliente.SCALE, Cliente.SCALE, Cliente.SCALE); 
            // fillRect cria retângulo (fruta)
            String string = "Pontos: " + snake.pontosObtidos + ", Tamanho Cobrinha: " + snake.tamanhoCobrinha + ", Tempo: " + snake.tempo / 20;
            g.setColor(Color.white);
            g.drawString(string, (int) (getWidth() / 2 - string.length() * 2.5f), 10);
            string = "Fim de Jogo!";
            if (snake.over) {
                g.drawString(string, (int) (getWidth() / 2 - string.length() * 2.5f), (int) snake.dim.getHeight() / 4);
            }
            string = "Jogo Pausado!";
            if (snake.jogoPausado && !snake.over) {
                g.drawString(string, (int) (getWidth() / 2 - string.length() * 2.5f), (int) snake.dim.getHeight() / 4);
            }
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
