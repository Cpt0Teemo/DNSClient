import java.util;

class DnsClient{

  public static void main(String[] args) {
    int sizeOfOptions = args.length;

    // Comamnd line input requires minimum 2 variables maximum 10 variables
    if(sizeOfOptions < 2 || sizeOfOptions > 10){
      System.out.println("Incorrect number of inputs...");
      System.exit(0);
    }
    List<String> arguments = Arrays.asList(args);
    DnsRequest request = new DnsRequest(asd);

  }
}
