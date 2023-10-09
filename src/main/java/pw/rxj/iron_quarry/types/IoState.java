package pw.rxj.iron_quarry.types;

import java.util.List;

public enum IoState {
    BLOCKED(0, "blocked"),
    ORANGE(1, "orange"),
    BLUE(2, "blue"),
    GREEN(3, "green"),
    PURPLE(4, "purple");

    private final int id;
    private final String name;

    private static final List<IoState> ALL = List.of(BLOCKED, ORANGE, BLUE, GREEN, PURPLE);

    private IoState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }

    public IoState next() {
        int nextId = id + 1 == ALL.size() ? 0 : id + 1;
        return from(nextId);
    }
    public IoState previous() {
        int prevId = id == 0 ? ALL.size() - 1 : id - 1;
        return from(prevId);
    }

    public static IoState from(String name){
        for (IoState ioState : ALL) {
            if(ioState.getName().equalsIgnoreCase(name)){
                return ioState;
            }
        }

        return null;
    }
    public static IoState from(int id){
        return ALL.get(id);
    }
}
