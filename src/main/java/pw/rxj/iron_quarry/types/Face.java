package pw.rxj.iron_quarry.types;

import net.minecraft.util.math.Direction;

import java.util.List;

public enum Face {
    BOTTOM(0, 1, "bottom"),
    TOP(1, 0, "top"),
    FRONT(2, 3, "front"),
    BACK(3, 2, "back"),
    LEFT(4, 5, "left"),
    RIGHT(5, 4, "right");

    private final int id;
    private final int idOpposite;
    private final String name;

    private static final List<Face> ALL = List.of(BOTTOM, TOP, FRONT, BACK, LEFT, RIGHT);

    private Face(int id, int idOpposite, String name) {
        this.id = id;
        this.idOpposite = idOpposite;
        this.name = name;
    }

    public Face getOpposite() {
        return ALL.get(this.idOpposite);
    }
    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }

    public Direction toDirection(Direction frontDirection) {
        switch (this) {
            case RIGHT -> {
                return frontDirection.rotateYClockwise();
            }
            case LEFT -> {
                return frontDirection.rotateYCounterclockwise();
            }
            case BACK -> {
                return frontDirection.getOpposite();
            }
            case FRONT -> {
                return frontDirection;
            }
            case TOP -> {
                return Direction.UP;
            }
            case BOTTOM -> {
                return Direction.DOWN;
            }
            default -> {
                return null;
            }
        }
    }

    public static Face from(int id){
        return ALL.get(id);
    }
    public static Face from(String name){
        for (Face face : ALL) {
            if(face.getName().equalsIgnoreCase(name)){
                return face;
            }
        }

        return null;
    }
    public static Face from(Direction direction, Direction frontDirection) {
        if(frontDirection.equals(direction)) {
            return Face.FRONT;
        } else if (frontDirection.rotateYClockwise().equals(direction)) {
            return Face.RIGHT;
        } else if (frontDirection.getOpposite().equals(direction)){
            return Face.BACK;
        } else if(frontDirection.rotateYCounterclockwise().equals(direction)){
            return Face.LEFT;
        } else if(direction.equals(Direction.DOWN)){
            return Face.BOTTOM;
        } else if(direction.equals(Direction.UP)) {
            return Face.TOP;
        }

        return null;
    }
}
