import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


//======================================================================
@SuppressWarnings("serial")
public class Snake
        extends JFrame
{
    private Board ps;
    int boardSize = 100;

    public Snake()
    {
        setTitle( "Snake" );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setLayout( new BorderLayout() );
        ps = new Board(boardSize);

        add( ps, BorderLayout.CENTER );
        pack();
        setVisible( true );
    }

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( () -> new Snake() );
    }
}

//======================================================================
@SuppressWarnings("serial")
class Board
        extends JPanel
        implements ComponentListener
{
    private Ball    ball;
    private Timer       timer;
    private BufferedImage   image;

    public Board( int boardSize)
    {
        Dimension dim = new Dimension( boardSize, boardSize); //width, height );

        image = new BufferedImage( boardSize, boardSize, BufferedImage.TYPE_INT_RGB);
        Graphics    g = image.getGraphics();
        g.setColor( Color.BLACK );
        g.fillRect( 0, 0, getWidth(), getHeight() );

        setPreferredSize( dim );
        setMinimumSize( dim );
        addComponentListener( this );
        int scaleSize = boardSize/10;


        ball = ( new Ball( scaleSize ) );

        timer = new Timer( 10,
                (evt) -> { ball.advance( ); repaint(); }
        );
    }

    public void paintComponent( Graphics g )
    {
        Graphics ig = image.getGraphics();
        ball.paint( ig );
        g.drawImage( image, 0, 0, null );
        timer.start();
    }

    @Override
    public void componentResized( ComponentEvent e ) { }

    @Override
    public void componentMoved( ComponentEvent e ) { }

    @Override
    public void componentShown( ComponentEvent e ) { }

    @Override
    public void componentHidden( ComponentEvent e ) { }
}

//======================================================================
class Ball
{
    private int         x, y;
    private int         width;
    private int         height;
    private Color       color;
    private position    curPosition;
    private position    nextPosition;
    private position    tail;
    private int boardSize = 100;
    private int scaleSize = boardSize/10;
    private int[][] board = new int[scaleSize][scaleSize];
    private boolean bNeedApple = true;
    private int snakeLength = 1;
    private char snakeDirection = 'N';

    public class position
    {
        int x = 0;
        int y = 0;
    }

    // Construct random ball
    public Ball( int newSize )
    {
        width = height = newSize;

        bNeedApple = true;
        // add new apple
        addAppleIfNeeded();

        curPosition = new position();
        nextPosition = new position();
        tail = new position();

        generatePosition(curPosition, scaleSize);
        x = curPosition.x;
        y = curPosition.y;

        tail.x = curPosition.x;
        tail.y = curPosition.y;

        // check if the apple is at this position
        if (appleFound(x, y))
        {
            // grow snake
            snakeLength++;

            bNeedApple = true;

            // add new apple
            addAppleIfNeeded();

            startSnake(curPosition, tail);
        }
        addSnake(curPosition.x, curPosition.y);

        //color = new Color( rand.nextInt() & 0xffffff );
    }

    public void generatePosition(position pos, int size)
    {
        pos.x = ThreadLocalRandom.current().nextInt(0, size );
        pos.y = ThreadLocalRandom.current().nextInt(0, size );
    }

    public void paint( Graphics g )
    {
        g.setColor( color );
        g.fillOval( x, y, width, height );
    }

    public void advance( )
    {
        if (!MoveSnakeSetup(curPosition, tail))
        {
            throw new ArrayIndexOutOfBoundsException("snake error");
        }

        x = curPosition.x;
        y = curPosition.y;
    }
    public boolean snakeFound(int pos1, int pos2)
    {
        int test = board[pos1][pos2];

        return (test == 1);
    }
    public boolean appleFound(int pos1, int pos2)
    {
        int test = board[pos1][pos2];

        return (test == 2);
    }
    public void addSnake(int x, int y)
    {
        board[x][y] = 2;
    }
    public boolean startSnake(position curPosition, position tail)
    {
        return MoveSnakeSetup(curPosition, tail);
    }
    public boolean MoveSnakeSetup(position curPosition, position tail)
    {
        position nextPosition = new position();
        nextPosition.x = curPosition.x;
        nextPosition.y = curPosition.y;

        if (!calculateNextMovePosition(nextPosition))
        {
            return false;
        }
        else
        {
            MoveSnake(curPosition, nextPosition, tail);
        }

        return true;
    }

    private void MoveSnake(position current, position next, position tail)
    {
        boolean growSnake = false;

        // see if there is an apple that increases our snake length
        if (FoundAppleAtNextPosition(next.x, next.y))
        {
            growSnake = true;
            bNeedApple = true;
        }

        // add snake to next position on board
        board[next.x][next.y] = 1;

        // check if we need to move the tail
        if (growSnake)
        {
            snakeLength++;
        }
        else
        {
            board[current.x][current.y] = 1;
            tail.x = current.x;
            tail.y = current.y;
        }

        // move done, update current pos to new position
        current.x = next.x;
        current.y = next.y;

        addAppleIfNeeded();
    }
    private boolean FoundAppleAtNextPosition(int x, int y)
    {
        return board[x][y] == 2;
    }

    public void addAppleIfNeeded()
    {
        if (bNeedApple)
        {
            boolean bDone = false;
            while (!bDone)
            {
                position test = new position();

                // rand 2 positions to put apple
                generatePosition(test, scaleSize);

                // ensure the snake body isn't blocking this position
                if (!snakeFound(test.x, test.y))
                {
                    board[test.x][test.y] = 2;
                    bNeedApple = false;
                    bDone = true;
                }
            }
        }
    }
    private void EndGameText(int x, int y, boolean offTheBoard, boolean touchedSnake, boolean noMovesLeft)
    {
        String message = "";

        if (offTheBoard)
        {
            message += "Snake has moved off the board to [" + x + "] [" + y + "]";
        }
        else if (touchedSnake)
        {
            message += "Snake has touched itself [" + x + "] [" + y + "]";
        }
        else if (noMovesLeft)
        {
            message += "Snake has no moves left [" + x + "] [" + y + "]";
        }
        else
        {
            message += "Snake movement has failed [" + x + "] [" + y + "]";
        }

        System.out.println(message);
        throw new ArrayIndexOutOfBoundsException(message);
    }
    private boolean calculateNextMovePosition(position pos)
    {
        switch (snakeDirection)
        {
            case 'N':
                pos.y++;
                break;
            case 'E':
                pos.x++;
                break;
            case 'S':
                pos.y--;
                break;
            case 'W':
                pos.x--;
                break;
        }

        boolean safeToMove = false;

        boolean isMoveOnTheBoard = IsPositionValid(pos.x, pos.y);

        boolean doesMoveTouchSnake = false;

        if (isMoveOnTheBoard)
        {
            CheckForSnake(pos.x, pos.y);
        }

        if (isMoveOnTheBoard && !doesMoveTouchSnake)
        {
            safeToMove = true;
        }
        return safeToMove;
    }

    private boolean IsPositionValid(int x, int y)
    {
        boolean xGood = (x < scaleSize) ? true : false;
        boolean yGood = (y < scaleSize) ? true : false;

        if (xGood && yGood)
        {
            return true;
        }

        EndGameText(x, y, true, false, false);
        return false;
    }
    private boolean CheckForSnake(int x, int y)
    {
        if(board[x][y] == 1)
        {
            EndGameText(x, y, false, true, false);
            return true;
        }

        return false;
    }
    private void pickDirection()
    {
        int num = ThreadLocalRandom.current().nextInt(1, 4 + 1);

        switch (num)
        {
            case 1:
                snakeDirection = 'N';
                break;
            case 2:
                snakeDirection = 'E';
                break;
            case '3':
                snakeDirection = 'S';
                break;
            case '4':
            default:
                snakeDirection = 'W';
                break;
        }
    }
}