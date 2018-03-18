import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;

public class TweetsFileWriter implements Runnable{

    final private BlockingQueue<String> queue;
    final private boolean isRestart;
    final private String path;

    public TweetsFileWriter(BlockingQueue<String> queue, boolean isRestart, String path) {
        this.queue = queue;
        this.isRestart = isRestart;
        this.path = path;
    }

    @Override
    public void run() {

        try {

            WriteFile();

        }
        catch (Exception ex){

            System.out.println("Writer Thread " + " Execute Error: " + ex.getClass() + " With messgage: " + ex.getMessage());

        }

    }

    private void WriteFile() throws Exception{

        try {

            while (true) {

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path,this.isRestart))){

                    bufferedWriter.write(queue.take());
                    bufferedWriter.newLine();

                    bufferedWriter.flush();

                }
                catch (Exception ex){
                    System.out.println("Writer Thread " + " WriteFile Writing Error: " + ex.getClass() + " With messgage: " + ex.getMessage());
                }

            }

        }
        catch (Exception ex){
            System.out.println("Writer Thread " + " WriteFile Error: " + ex.getClass() + " With messgage: " + ex.getMessage());
        }



    }
}
