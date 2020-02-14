import java.util.ArrayList;
import java.util.List;

public class Record {
    public List<String> name;
    public ServerType type;
    public int TTL;
    public List<String> labels;
    public int preference;
    public boolean isAuthoritative;
    public boolean isAdditional;

    public Record()
    {
        name = new ArrayList<>();
        type = ServerType.A;
        TTL = 0;
        labels = new ArrayList<>();
        isAuthoritative = false;
        isAdditional = false;
    }

    public String displayInfo()
    {
        return (type
                + " " + joinLabels()
                + (this.type == ServerType.MX ? (" " + this.preference) : "")
                + " " + this.TTL
                + " " + (this.isAuthoritative ? "auth" : "noauth") + "\n");
    }

    public String joinLabels()
    {
        if(this.type != ServerType.AAAA)
            return String.join(".", labels);

        String ipv6 = "";
        for(String label: labels)
        {
            String hex = Integer.toHexString(Integer.parseInt(label));
            if (!hex.equals("0"))
                ipv6 += hex + ":";
            else
                ipv6 += ":";
        }
        ipv6 = ipv6.replaceAll("(:){2,}", "::");
        return ipv6.substring(0, ipv6.length() - 1);
    }
}
