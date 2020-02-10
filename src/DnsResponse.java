import java.util.ArrayList;
import java.util.List;

public class DnsResponse {
    private int id;
    private boolean isAuthoritative = false;
    private List<String> labels = new ArrayList<>();
    private List<String> responseDomainLabels = new ArrayList<>();
    private ServerType responseType;
    private List<String> ip = new ArrayList<>();

    public DnsResponse(byte[] data) throws Exception {
        parseHeader(data);
        parseData(data);
    }

    private void parseHeader(byte[] data) throws Exception
    {
        this.id = readIntByte(data, 0, 2);
        //Check is response
        if(!checkBitAtPosition(1 ,data[2]))
            throw new Exception();
        //Check is server is authoritative
        if(checkBitAtPosition(6 ,data[2]))
            this.isAuthoritative = true;
        //Check is serve handles recursive queries
        if(!checkBitAtPosition(1 ,data[3]))
            throw new Exception("Server does not handle recursive calls");
        //Check response status code
        int response = data[3] & 8;
        if(response != 0)
        {
            if(response == 3)
                throw new Exception("Not found");
            else
                throw new Exception();
        }
    }

    private void parseData(byte[] data) throws Exception
    {
        var index = 12;
        index = getLabels(data, index, this.labels);
        //Check type
        index += 2;
        getType(data[index]);
        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new Exception();

        index = getLabels(data, ++index, this.responseDomainLabels);
        index += 2;
        getType(data[index]);

        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new Exception();

        //Check TTL
        index++;
        int TTl = readIntByte(data, index, 4);
        index += 4;

        int length = readIntByte(data, index, 2);
        index += 2;


        switch (this.responseType)
        {
            case A:
                getIp(data, index);
                break;
            case MX:
                break;
            case NS:
                break;
            case CNAME:
                break;
        }
    }

    private int readIntByte(byte[] data, int index, int size)
    {
        int value = 0;
        for(int i = size; i > 0; i--)
        {
            value += (Byte.toUnsignedInt(data[index++]) << (8*(i-1)));
        }
        return value;
    }

    private void getIp(byte[] data, int index)
    {
        for(int i = 0; i < 4; i++)
        {
            this.ip.add(String.valueOf(Byte.toUnsignedInt(data[index + i])));
        }
    }

    private int getLabels(byte[] data, int index, List<String> labels)
    {
        int labelLength = 0;
        String label;

        while(true)
        {
            label = "";
            labelLength = Byte.toUnsignedInt(data[index]);
            if(labelLength == 0)
                break;
            if(labelLength >= 192)
            {
                int offset = ((labelLength - 192)<<8) + Byte.toUnsignedInt(data[++index]);
                getLabels(data, offset, labels);
                return index;
            }
            index++;
            for(int i = 0; i < labelLength; i++)
            {
                label += (char) data[index];
                index++;
            }
            labels.add(label);
        }
        return index;
    }

    private void getType(byte datum) throws Exception
    {
        switch (datum)
        {
            case 1:
                this.responseType = ServerType.A;
                break;
            case 15:
                this.responseType = ServerType.MX;
                break;
            case 2:
                this.responseType = ServerType.NS;
                break;
            case 5:
                this.responseType = ServerType.CNAME;
                break;
            default:
                throw new Exception("Type not accepted");

        }
    }

    private boolean checkBitAtPosition(int position, byte data)
    {
        int bit = data >> (8 - position);

        if((bit & 1) == 1)
            return true;

        return false;
    }

    public boolean checkResponseWithRequest(DnsRequest request)
    {
    if( this.id != request.getId() )
        return false;
    if( this.responseType != request.getServerType() )
        return false;
    if( !String.join(".", this.responseDomainLabels).equals(request.getDomainName()) )
        return false;

    return true;
    }
}
