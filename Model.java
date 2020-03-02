package com.javarush.task.Projects.Game2048;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score;
    int maxTile;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        score = 0;
        maxTile = 0;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty())
                    emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    //в рандомную пустую ячейку добавляем плитку
    private void addTile(){
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            Tile randomEmptyTile = emptyTiles.get((int) (emptyTiles.size() * Math.random()));
            randomEmptyTile.value = Math.random() < 0.9 ? 2 : 4; //по условию на 9 двоек должна приходиться 1 четверка
        }
    }

    void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    public boolean canMove(){
        if (!getEmptyTiles().isEmpty())
            return true;

        //если по соседтсву есть две одинаковые плитки
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTiles[i][j].value == gameTiles[i][j-1].value)
                    return true;
            }
        }
        
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTiles[j][i].value == gameTiles[j-1][i]. value)
                    return true;
            }
        }
        return false;
    }

    //перенос нулей в правую часть
    private boolean compressTiles(Tile[] tiles){
        int count = 0;  // Count of non-zero elements
        boolean tileEdited = false;

        // Traverse the array. If element encountered is
        // non-zero, then replace the element at index 'count'
        // with this element
        for (int i = 0; i < tiles.length; i++)
            if (tiles[i].value != 0) {
                tiles[count++].value = tiles[i].value; // here count is incremented
                if (count - 1 != i) //если мы переставляем элемент, значит было изменение
                    tileEdited = true;
            }

        // Now all non-zero elements have been shifted to
        // front and 'count' is set as index of first 0.
        // Make all elements 0 from count to end.
        while (count < tiles.length)
            tiles[count++].value = 0;
        
        return tileEdited;
    }

    private boolean mergeTiles(Tile[] tiles){
        compressTiles(tiles);
        boolean tileEdited = false;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value && tiles[i].value != 0){
                tiles[i].value *= 2;
                tiles[i + 1].value = 0;
                compressTiles(tiles);
                tileEdited = true;

                score += tiles[i].value;
                if (tiles[i].value > maxTile)
                    maxTile = tiles[i].value;
            }
        }
        return tileEdited;
    }

    void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0:
                up();
                break;
            case 1:
                down();
                break;
            case 2:
                right();
                break;
            case 3:
                left();
                break;
        }
    }
    
    void left(){
        if (isSaveNeeded)
            saveState(gameTiles);

        boolean tilesEdited = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]))//вызываем методы и заодно проверяем, был ли сдвиг или слияние. Если да, добавляем новую плитку в рандомном месте
                tilesEdited = true;
        }
        if (tilesEdited) addTile();
        isSaveNeeded = true;
    }

    void right(){
        saveState(gameTiles);
        for (int i = 0; i < 4; i++) {
            if (i == 2)
                left();
            rightRotate();
        }
    }

    void down(){
        saveState(gameTiles);
        for (int i = 0; i < 4; i++) {
            if (i == 1)
                left();
            rightRotate();
        }
    }

    void up(){
        saveState(gameTiles);
        for (int i = 0; i < 4; i++) {
            if (i == 3)
                left();
            rightRotate();
        }
    }

    private void rightRotate(){
        Tile[][] newTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++)
                newTiles[j][FIELD_WIDTH - 1 - i] = gameTiles[i][j];
        }
        gameTiles = newTiles;
    }

    private void saveState(Tile[][] tiles){
        Tile[][] fieldToSave = new Tile[tiles.length][tiles[0].length];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                fieldToSave[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(fieldToSave);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if (!previousStates.isEmpty() & !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public boolean hasBoardChanged(){
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value)
                    return true;
            }
        }
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        move.move();
        MoveEfficiency moveEfficiency = hasBoardChanged() ? new MoveEfficiency(getEmptyTiles().size(), score, move) :
                                                            new MoveEfficiency(-1, 0, move);
        rollback();
        return moveEfficiency;
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(this::left));
        priorityQueue.add(getMoveEfficiency(this::right));
        priorityQueue.add(getMoveEfficiency(this::up));
        priorityQueue.add(getMoveEfficiency(this::down));
        Objects.requireNonNull(priorityQueue.poll()).getMove().move();
    }
}
