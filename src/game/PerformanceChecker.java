package game;

import controllers.pacman.IPacManController;
import game.core.Game;

public class PerformanceChecker {
	public PerformanceChecker(){}


    public void CheckCopy(Game game) {
        int iterations = 1000;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime(); 
            Game copy = game.copy();            
            long endTime = System.nanoTime();   
            
            totalTime += (endTime - startTime); 
        }
        double averageTime = (double) totalTime / iterations;
        System.out.println("Average time to copy game: " + averageTime + " ns");
    }

    public void CheckAdvance(Game game) {
        int iterations = 1000;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime(); 

            int direction = game.getCurPacManDir();

            // int[] directions=game.getPossiblePacManDirs(false);
            // int direction = directions[game.rand().nextInt(directions.length)];

            game.advanceGame(direction);
            long endTime = System.nanoTime();   
            
            totalTime += (endTime - startTime); 
        }
        double averageTime = (double) totalTime / iterations;
        System.out.println("Average time to advance game: " + averageTime + " ns");
    }

    public void CheckAStar(IPacManController pacManController, Game game, long due) {
        int iterations = 1000;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime(); 
            pacManController.tick(game.copy(), due);
            long endTime = System.nanoTime();  
            
            totalTime += (endTime - startTime); 
        }
        double averageTime = (double) totalTime / iterations;
        System.out.println("Average time for A*: " + averageTime + " ns");
    }


}
