import java.util.ArrayList;
import java.util.List;

public class DnsResponse {
    private int id;
    private boolean isAuthoritative = false;
    private List<String> labels = new ArrayList<>();
    private ServerType questionType;
    private int ansCount = 0;
    private int authCount = 0;
    private int additionalCount = 0;
    private List<Response> responses = new ArrayList<>();



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
        //if(!checkBitAtPosition(1 ,data[3]))
            //throw new DnsResponseException("ERROR: Server does not handle recursive calls");
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
        //Read number of authority answers
        this.authCount = readIntByte(data, 8, 2);
        //Read number of addition record
        this.additionalCount = readIntByte(data, 10, 2);

    }

    private void parseData(byte[] data) throws DnsResponseException
    {
        var index = 12;
        index = getLabels(data, index, this.labels);
        //Check type
        index += 2;
        this.questionType = getType(data[index]);
        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new DnsResponseException("ERROR: Query class is not IN");

        for(int i = 0; i < this.ansCount; i++)
        {
            Response response = new Response();
            this.responses.add(response);
            index = parseResponse(data, index, response);
        }
        for(int i = 0; i < this.authCount; i++)
        {
            Response response = new Response();
            response.isAuthoritative = true;
            this.responses.add(response);
            index = parseResponse(data, index, response);
        }
        for(int i = 0; i < this.additionalCount; i++)
        {
            Response response = new Response();
            response.isAdditional = true;
            this.responses.add(response);
            index = parseResponse(data, index, response);
        }
    }

    private int parseResponse(byte[] data, int index, Response response) throws DnsResponseException
    {

        index = getLabels(data, ++index, response.name);

        index += 2;
        response.type = getType(data[index]);

        //Check class
        if(data[++index] != 0 || data[++index] != 1)
            throw new DnsResponseException("ERROR: Response class is not IN");

        //Check TTL
        index++;
        response.TTL = readIntByte(data, index, 4);
        index += 4;

        int length = readIntByte(data, index, 2);
        index += 2;


        switch (response.type)
        {
            case A:
                index = getIp(data, index, response.labels);
                break;
            case AAAA:
                index = getIpv6(data, index, response.labels);
                break;
            case MX:
                index = getMxInformation(data, index, response);
                break;
            case NS:
                index = getLabels(data, index, response.labels);
                break;
            case CNAME:
                index = getLabels(data, index, response.labels);
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

    private int getIp(byte[] data, int index, List<String> ip)
    {
        for(int i = 0; i < 4; i++)
        {
            ip.add(String.valueOf(Byte.toUnsignedInt(data[index + i])));
        }
        return index + 3;
    }

    private int getIpv6(byte[] data, int index, List<String> ip6)
    {
        for(int i = 0; i < 8; i++)
        {
            ip6.add(String.valueOf(readIntByte(data, index, 2)));
            index += 2;
        }
        return --index;
    }

    private int getMxInformation(byte[] data, int index, Response response)
    {
        response.preference = readIntByte(data, index, 2);
        index += 2;
        return getLabels(data, index, response.labels);
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

    private ServerType getType(byte datum) throws DnsResponseException
    {
        switch (datum)
        {
            case 1:
                return ServerType.A;
            case 15:
                return ServerType.MX;
            case 2:
                return ServerType.NS;
            case 5:
                return ServerType.CNAME;
            case 28:
                return ServerType.AAAA;
            default:
                return ServerType.OTHER;
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
    if( this.questionType != request.getServerType() )
        return false;
    return true;
    }

    public String displayInformation()
    {
        List<Response> answerResponses= new ArrayList<Response>();
        List<Response> additionalResponses= new ArrayList<Response>();
        String result = "";
        for(Response response: responses) {
            if(!response.isAdditional)
                answerResponses.add(response);
            else
                additionalResponses.add(response);
        }

        result += "\n ***Answer Section (" + answerResponses.size() + " records)***\n";
        for(Response response: answerResponses) {
            result += response.displayInfo();
        }
        result += "\n ***Additional Section (" + additionalResponses.size() + " records)***\n";
        for(Response response: additionalResponses) {
            result += response.displayInfo();
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
