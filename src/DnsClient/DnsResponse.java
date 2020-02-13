package DnsClient;

import java.util.ArrayList;
import java.util.List;

public class DnsResponse {
    private int id;
    private boolean isAuthoritative = false;
    private List<String> labels = new ArrayList<>();
    private List<String> responseDomainLabels = new ArrayList<>();
    private ServerType responseType;
    private int ansCount = 0;
    private List<Integer> TTl = new ArrayList<Integer>();
    //Type A
    private List<List<String>> ip = new ArrayList<>();
    //Type MX
    private List<Integer> preference = new ArrayList<>();
    private List<List<String>> mxDomainLabels = new ArrayList<>();
    //Type NS
    private List<List<String>> nsDomainLabels = new ArrayList<>();
    //Type CNAME
    private List<List<String>> cnameDomainLabels = new ArrayList<>();



    public DnsResponse(byte[] data) throws DnsResponseException {
        parseHeader(data);
        parseData(data);
    }

    private void parseHeader(byte[] data) throws DnsResponseException
    {
        this.id = readIntByte(data, 0, 2);
        //Check is answer
        if(!checkBitAtPosition(1 ,data[2]))
            throw new DnsResponseException("ERROR: The response is not an answer");
        //Check is server is authoritative
        if(checkBitAtPosition(6 ,data[2]))
            this.isAuthoritative = true;
        //Check is serve handles recursive queries
        if(!checkBitAtPosition(1 ,data[3]))
            throw new DnsResponseException("ERROR: Server does not handle recursive calls");
        //Check response status code
        int response = data[3] & 8;
        if(response != 0)
        {
            if(response == 1)
                throw new DnsResponseException("ERROR: The name server was unable to interpret the query");
            else if(response == 2)
                throw new DnsResponseException("ERROR: The name server was unable to process this query due to a problem with the name server");
            else if(response == 3)
                throw new DnsResponseException("NOTFOUND: Meaningful only for responses from an authoritative name server, this code\n" +
                        "signifies that the domain name referenced in the query does not exist");
            else if(response == 4)
                throw new DnsResponseException("ERROR: The name server does not support the requested kind of query");
            else if(response == 5)
                throw new DnsResponseException("ERROR: The name server refuses to perform the requested operation for policy reasons");
            else
            throw new DnsResponseException("ERROR: Unhadled status code");
        }
        //Check only one question
        if(readIntByte(data, 4, 2) != 1)
            throw new DnsResponseException("The response has more than one question");

        //Read number of answers
        this.ansCount = readIntByte(data, 6, 2);

    }

    private void parseData(byte[] data) throws DnsResponseException
    {
        var index = 12;
        index = getLabels(data, index, this.labels);
        //Check type
        index += 2;
        getType(data[index]);
        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new DnsResponseException("ERROR: Response class is not");

        for(int i = 0; i < this.ansCount; i++)
            index = parseResponse(data, index, i);
    }

    private int parseResponse(byte[] data, int index, int answerCount) throws DnsResponseException
    {
        if(this.responseDomainLabels.isEmpty())
            index = getLabels(data, ++index, this.responseDomainLabels);
        else
            index = getLabels(data, ++index, new ArrayList<String>());
        index += 2;
        getType(data[index]);

        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new DnsResponseException("ERROR: Query is no an IP");

        //Check TTL
        index++;
        this.TTl.add(readIntByte(data, index, 4));
        index += 4;

        int length = readIntByte(data, index, 2);
        index += 2;


        switch (this.responseType)
        {
            case A:
                this.ip.add(new ArrayList<>());
                index = getIp(data, index, answerCount);
                break;
            case MX:
                this.mxDomainLabels.add(new ArrayList<>());
                index = getMxInformation(data, index, answerCount);
                break;
            case NS:
                this.nsDomainLabels.add(new ArrayList<>());
                index = getLabels(data, index, this.nsDomainLabels.get(answerCount));
                break;
            case CNAME:
                this.cnameDomainLabels.add(new ArrayList<>());
                index = getLabels(data, index, this.cnameDomainLabels.get(answerCount));
                break;
        }
        return index;
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

    private int getIp(byte[] data, int index, int ansCount)
    {
        for(int i = 0; i < 4; i++)
        {
            this.ip.get(ansCount).add(String.valueOf(Byte.toUnsignedInt(data[index + i])));
        }
        return index + 4;
    }

    private int getMxInformation(byte[] data, int index, int ansCount)
    {
        this.preference.add(readIntByte(data, index, 2));
        index += 2;
        return getLabels(data, index, this.mxDomainLabels.get(ansCount));
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

    private void getType(byte datum) throws DnsResponseException
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
                throw new DnsResponseException("Type not accepted");

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

    public String displayInformation()
    {
        switch (this.responseType)
        {
            case A:
                return joinDisplayStrings(ServerType.A.toString(), this.ip);
            case MX:
                return joinDisplayStrings(ServerType.MX.toString(), this.mxDomainLabels);
            case NS:
                return joinDisplayStrings(ServerType.NS.toString(), this.nsDomainLabels);
            case CNAME:
                return joinDisplayStrings(ServerType.CNAME.toString(), this.cnameDomainLabels);       }
        return "";
    }

    private String joinDisplayStrings(String type, List<List<String>> listOfStrings)
    {
        String result = "";
        int index = 0;
        for (List<String> labels: listOfStrings) {
            result += (type + " " + String.join(".", labels)
                    + (this.responseType == ServerType.MX ? (" " + this.preference.get(index)) : "")
                    + " " + this.TTl.get(index)
                    + " " + (this.isAuthoritative ? "auth" : "noauth" + "\n"));
            index++;
        }
        return result;
    }

    public class DnsResponseException extends Exception
    {
        public DnsResponseException(String message) {
            super(message);
        }
    }
}
