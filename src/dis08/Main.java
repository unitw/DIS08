package dis08;



/**
 * @author marius
 *
 */
public class Main {

    public static void main(String[] args) {
        try {
            Apriori apriori = new Apriori("C:/tmp1/transactionsLarge.txt");
            apriori.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
