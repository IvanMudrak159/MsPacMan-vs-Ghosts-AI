import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import controllers.pacman.PacManControllerBase;
import game.core.Game;
import game.core.Game.DM;
import game.core.GameView;

public final class IvanMudrak extends PacManControllerBase {	

    private int safetyFee = 3;
    private int ghostDistance = 500;
    private int ghostEdibleReward = 500;
    private int ghostNonEdibleFee = 2500;

    @Override
    public void tick(Game game, long timeDue) {
        Color pathColor = Color.GREEN;
        boolean isTimeout = false;
        int start = game.getCurPacManLoc();

        PriorityQueue<PriorityNode> fringe = new PriorityQueue<>();
        fringe.add(new PriorityNode(start, 0, game));

        HashMap<Integer, Integer> visitedCosts = new HashMap<>();
        visitedCosts.put(start, 0);

        HashMap<Integer, Integer> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        int goal = GetGoal(start, game);
        
        if(game.getNumActivePills() == 1 && game.getDistanceToNearestPill() < 3) {
            pacman.set(game.getCurPacManDir());
            return;
        }

        while (!fringe.isEmpty()) {

            if (System.currentTimeMillis() >= timeDue) {
                isTimeout = true;
                break;
            }
            
            PriorityNode currentPriorityNode = fringe.poll();
            Game currentGameState = currentPriorityNode.game;

            if (currentPriorityNode.node == goal) {
                break;
            }

            int[] possibleActions = currentGameState.getPossiblePacManDirs(true);
            for (int action : possibleActions) {
                Game nextState = currentGameState.copy();
                nextState.advanceGame(action);

                int nextNode = nextState.getCurPacManLoc();
                int currentCost = visitedCosts.get(currentPriorityNode.node);
                int newNextCost = currentCost + currentGameState.getManhattanDistance(currentPriorityNode.node, nextNode);

                Integer nextCost = visitedCosts.get(nextNode);
                if (nextCost == null || newNextCost < nextCost) {
                    visitedCosts.put(nextNode, newNextCost);

                    int priority = newNextCost + Heuristic(currentGameState, nextNode, goal);
                    fringe.add(new PriorityNode(nextNode, priority, nextState));
                    cameFrom.put(nextNode, currentPriorityNode.node);
                }
            }
        }

        GameView.addLines(game, Color.BLUE, start, goal);

        if(!cameFrom.containsKey(goal)) {
            goal = FindNearestPoint(start, goal, cameFrom);
            pathColor = Color.YELLOW;
        }

        if(isTimeout) {
            pathColor = Color.RED;
        }

        int nextDirection;
        Integer nextNode = GetNextNode(start, goal, cameFrom);
        if(nextNode == start) {
            nextDirection = game.getCurPacManDir();
        } else {
            nextDirection  = game.getNextPacManDir(nextNode, true, DM.MANHATTAN);
        }
        pacman.set(nextDirection);
        ShowPath(game, start, goal, cameFrom, pathColor);
    }
    

    private void ShowPath(Game game, int start, int goal, HashMap<Integer, Integer> cameFrom, Color pathColor) {
        int[] path = GetPath(start, goal, cameFrom);
        GameView.addPoints(game, pathColor, path);
    }

    private Integer GetNextNode(int start, int goal, HashMap<Integer, Integer> cameFrom) {
        if(goal == start) {
            return start;
        }

        int currentNode = goal;
        int previousNode = -1;
        while (currentNode != start) {
            previousNode = currentNode;
            currentNode = cameFrom.get(currentNode);
        }
        return previousNode;
    }

    private int FindNearestPoint(int start, int goal, HashMap<Integer, Integer> cameFrom) {
        ArrayList<Integer> nearestPoints = new ArrayList<>();
        int minDistance = Integer.MAX_VALUE;
        
        for (Map.Entry<Integer, Integer> entry : cameFrom.entrySet()) {
            int point = entry.getKey();
            int distance = game.getManhattanDistance(point, goal);
            
            if (distance < minDistance) {
                nearestPoints.clear();
                nearestPoints.add(point);
                minDistance = distance;   
            }   else if (distance == minDistance) {
                nearestPoints.add(point);
            }
        }

        return GetNearestPoint(start, nearestPoints);
    }   

    private int GetNearestPoint(int start, ArrayList<Integer> points) {
        int nearestPointToStart = points.get(0);
        int minDistanceToStart = game.getManhattanDistance(nearestPointToStart, start);
    
        for (int point : points) {
            int distanceToStart = game.getManhattanDistance(point, start);
            
            if (distanceToStart < minDistanceToStart) {
                nearestPointToStart = point;
                minDistanceToStart = distanceToStart;
            }
        }
        return nearestPointToStart;
    }

	private int[] GetPath(int start, int goal, HashMap<Integer, Integer> cameFrom) {
        ArrayList<Integer> pathList = new ArrayList<>();
        int currentNode = goal;
        pathList.add(goal);

        while (currentNode != start) {
            currentNode = cameFrom.get(currentNode);
            pathList.add(currentNode);
        }

        int[] path = new int[pathList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            path[i] = pathList.get(i);
        }
        return path;
	}

    private int GetGoal(int start, Game game) {
        int goal = game.getTarget(start, getAllPillIndicesActive(game), true, DM.MANHATTAN);
        return goal;
    }

    private int Heuristic(Game game, int nextNodePos, int goalPos) {

        int H_GoalDistance = GetGoalDistance(game, nextNodePos, goalPos);
        int H_Ghost = GetGhostHeuristic(game);
        // int H_safety = GetSafetyHeuristic(game);
        return H_GoalDistance + H_Ghost;
    }

    private int GetGoalDistance(Game game, int nextNodePos, int goalPos) {

        return game.getManhattanDistance(nextNodePos, goalPos);
    }

    private int GetGhostHeuristic(Game game) {

        int start = game.getCurPacManLoc();
        int ghostDistances[] = new int[Game.NUM_GHOSTS];
        for(int i = 0; i < Game.NUM_GHOSTS; i++) 
        {
            int distance = game.getManhattanDistance(start, game.getCurGhostLoc(i));
            ghostDistances[i] = distance;
        }

        int H_ghost = 0;
        for (int i = 0; i < ghostDistances.length; i++) {

            if(ghostDistances[i] < ghostDistance) {
                if (game.isEdible(i)) {
                    if(ghostDistances[i] < 3) 
                    {
                        H_ghost -= 100000;
                        continue;
                    }
                    H_ghost -= ghostEdibleReward * 1 / ghostDistances[i];
                } 
                else {
                    if(ghostDistances[i] < 3) 
                    {
                        H_ghost += 10000000;
                        continue;
                    }
                    H_ghost += ghostNonEdibleFee * 1 / ghostDistances[i];
                }
            }
        }
        return H_ghost;
    }

    @SuppressWarnings("unused")
    private int GetSafetyHeuristic(Game game) {
        int H_safety = (game.getPossiblePacManDirs(false).length < 2 ) ? safetyFee : 0;
        return H_safety;
    }

    public int[] getAllPillIndicesActive(Game game){
        int[] activePills = game.getPillIndicesActive();
		int[] activePowerPills = game.getPowerPillIndicesActive();
		ArrayList<Integer> edibleGhosts = getEdibleGhostsPositions(game);
		
		int fruitLoc = game.getFruitLoc();
		int fruitIndexSize = 0;
		if(fruitLoc != -1) {
			fruitIndexSize = 1;
		}

		int targetsSize = activePills.length+activePowerPills.length + edibleGhosts.size() + fruitIndexSize;

		int[] targetsArray=new int[targetsSize];
		
		for(int i=0;i<activePills.length;i++)
			targetsArray[i]=activePills[i];
		
		for(int i=0;i<activePowerPills.length;i++)
			targetsArray[activePills.length+i]=activePowerPills[i];		

		for (int i = 0; i < edibleGhosts.size(); i++)
			targetsArray[activePills.length + activePowerPills.length + i] = edibleGhosts.get(i);
			
		if(fruitLoc != -1) {
			targetsArray[targetsSize - 1] = fruitLoc;
		}
	

		return targetsArray;
    }

	public ArrayList<Integer> getEdibleGhostsPositions(Game game) {
		ArrayList<Integer> edibleGhosts = new ArrayList<>();
		for (int i = 0; i < Game.NUM_GHOSTS; i++) {
			if (game.isEdible(i)) {
				edibleGhosts.add(game.getCurGhostLoc(i));
			}
		}
		return edibleGhosts;	
	}
}