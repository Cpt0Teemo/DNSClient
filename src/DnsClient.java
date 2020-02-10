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
    byte[] sendData = null;
    byte[] receiveData = new byte[1024];


    try {
      sendData = request.createDnsRequest();
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }

    var retries = request.getMax_retries();
    while(retries > 0)
    {
      try {
        InetAddress IPAddress = InetAddress.getByAddress(request.getServerIpBytes());
        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(request.getTimeout() * 1000);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, request.getPort());

        clientSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        clientSocket.receive(receivePacket);


        var response = new DnsResponse(receivePacket.getData());
        var truth = response.checkResponseWithRequest(request);

        System.out.println("FROM SERVER: " + truth);
        clientSocket.close();
        break;
      } catch (Exception e) {
        System.out.println(e.getMessage());
        retries--;
      } finally {
        if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
      }
    }
  }
}
