package il.ac.bgu.se.bp.socket.state;
import java.io.Serializable;
import java.util.*;

public class BThreadInfo implements Serializable {
    private static final long serialVersionUID = 2208145820539894522L;

    private String name;
    private Map<Integer, Map<String, String>> env;
    private EventInfo wait;
    private EventInfo blocked;
    private Set<EventInfo> requested;

    public BThreadInfo() {
    }

    public BThreadInfo(String name, Map<Integer, Map<String, String>> env) {
        this.name = name;
        this.env = env;
        this.requested = new HashSet<>();
    }

    public BThreadInfo(String name, Map<Integer, Map<String, String>> env, EventInfo wait, EventInfo blocked, Set<EventInfo> requested) {
        this.name = name;
        this.env = env == null ? new HashMap<>() : env;
        this.wait = wait;
        this.blocked= blocked;
        this.requested = requested;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Map<String, String>> getEnv() {
        return env;
    }

    public void setEnv(Map<Integer, Map<String, String>> env) {
        this.env = env;
    }

    public EventInfo getWait() {
        return wait;
    }

    public void setWait(EventInfo wait) {
        this.wait = wait;
    }

    public EventInfo getBlocked() {
        return blocked;
    }

    public void setBlocked(EventInfo blocked) {
        this.blocked = blocked;
    }

    public Set<EventInfo> getRequested() {
        return requested;
    }

    public void setRequested(Set<EventInfo> requested) {
        this.requested = requested;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BThreadInfo that = (BThreadInfo) o;
        return name.equals(that.name) &&
                env.equals(that.env) &&
                Objects.equals(wait,that.wait) &&
                Objects.equals(blocked,that.blocked) &&
                Objects.equals(requested,that.requested);
    }

    public String prettier(String ... prefix) {
        String pref = prefix != null && prefix.length > 0 ? prefix[0] : "";

        List<String> envS = new LinkedList<>();

        for (Map.Entry e : env.entrySet()) {
            envS.add(e.getKey() + ":" + e.getValue());
        }

        return pref + "BThreadInfo{" + "\n" +
                pref + "\tname= '" + name + "\'\n" +
                pref + "\tenv= [" + String.join("\n\t" + pref, envS) + "],\n" +
                pref + "\twait= " + wait + ",\n" +
                pref + "\tblocked= " + blocked + ",\n" +
                pref + "\trequested= " + requested + ",\n" +
                pref + '}';
    }

    @Override
    public String toString() {
        return "BThreadInfo{" +
                "name='" + name + '\'' +
                ", env=" + env +
                ", wait=" + wait +
                ", blocked=" + blocked +
                ", requested=" + requested +
                '}';
    }
}