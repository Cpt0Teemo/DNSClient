import java.util.Arrays;
import java.util.Iterator;
import java.net.*;

class DnsClient{

  public static void main(String[] args) {
    int sizeOfOptions = args.length;

    // Command line input requires minimum 2 variables maximum 10 variables
    if(sizeOfOptions < 2 || sizeOfOptions > 10){
      System.out.println("ERROR: Incorrect number of inputs...");
      System.exit(0);
    }
    
    Iterator<String> arguments = Arrays.stream(args).iterator();
        
    DnsRequest request = new DnsRequest(arguments);

    sendUdpRequest(request);
  }

  private static void sendUdpRequest(DnsRequest request)
  {
    DatagramSocket clientSocket = null;
    try {
      InetAddress IPAddress = InetAddress.getByAddress(request.getServerIpBytes());
      clientSocket = new DatagramSocket();

      byte[] sendData = request.createDnsRequest();
      byte[] receiveData = new byte[1024];

      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, request.getPort());

      clientSocket.send(sendPacket);

      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      clientSocket.receive(receivePacket);

      var response = new DnsResponse(receivePacket.getData());
      var truth = response.checkResponseWithRequest(request);

      System.out.println("FROM SERVER: " + truth );
      clientSocket.close();
    }catch(Exception e)
    {
      System.out.println(e.getMessage());
    }finally {
      if(clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
    }
  }
}
