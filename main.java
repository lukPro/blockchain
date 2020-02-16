package blockchain;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

import java.security.MessageDigest;

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Block implements Serializable{
    private int id;
    private long timeStamp;
    private String previousHash;
    private String hash;
    private long magicNumber;

    public Block(int id, String previousBlockHash) {
        this.id = id;
        timeStamp = new Date().getTime();
        this.previousHash = previousBlockHash;
        hash = StringUtil.applySha256(String.valueOf(id) + timeStamp + "randomVals#!@#$%^");
    }

    public Block(int id, String previousHash, long magicNumber) {
        this.id = id;
        timeStamp = new Date().getTime();
        this.previousHash = previousHash;
        this.magicNumber = magicNumber;
        hash = StringUtil.applySha256(magicNumber + "thisIsRandomInput!@#345HJKa-");
    }

    @Override
    public String toString() {
        return "Block:\n" +
                "Id: " + this.id +"\n" +
                "Timestamp: " + timeStamp + "\n" +
                "Magic number: " + this.magicNumber + "\n" +
                "Hash of the previous block: \n" + this.previousHash + "\n" +
                "Hash of the block: \n" + hash;
    }

    public int getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getMagicNumber() { return magicNumber; }
}

class BlockChain implements Serializable {
    private static final long serialVersionUID = 1L;

    private int size;
    private ArrayList<Block> blockChain;


    public BlockChain(int size) {
        this.size = size;
        blockChain = new ArrayList<>(size);
    }

    public ArrayList<Block> getBlockChain() {
        return blockChain;
    }

    public void addBlock() {
        if (blockChain.isEmpty()) {
            //System.out.println("first block");
            blockChain.add(new Block(1, "0"));
        } else {
            int numberOfLastBlock = blockChain.size();
            //System.out.println("blockChain size = " + numberOfLastBlock);
            blockChain.add(new Block(blockChain.get(numberOfLastBlock-1).getId() + 1, blockChain.get(numberOfLastBlock-1).getHash()));
        }
    }

    public void addBlock (int zerosInHash) {
        //Block tmpBlock = new Block()
        boolean stop = false;

        if (blockChain.isEmpty()) {
            Block block;
            do {
                int magicNumber = new Random().nextInt(Integer.MAX_VALUE);
                block = new Block(1, "0", magicNumber);
                stop = validateHash(block.getHash(), zerosInHash);
            } while (!stop);
            blockChain.add(block);
        }
        else {
            Block block;
            do {
                int magicNumber = new Random().nextInt(Integer.MAX_VALUE);
                block = new Block(blockChain.size() + 1, blockChain.get(blockChain.size()-1).getHash(), magicNumber);
                stop = validateHash(block.getHash(), zerosInHash);
            } while (!stop);
            blockChain.add(block);
        }
    }

    private boolean validateHash(String hash, int numberOfZerosInHash) {
        for (int i = 0; i < numberOfZerosInHash; i++) {
            if (hash.charAt(i) == '0') {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean validateBlockChain() {

        for (int i = 0; i < blockChain.size()-1; i++) {
            if (i == 0) {
                if (blockChain.get(i).getId() == 1 && blockChain.get(i).getPreviousHash().equals("0")) {
                    continue;
                } else {
                    //System.out.println("zly pierwszy index");
                    return false;
                }
            }
            if (blockChain.get(i).getHash().equals(blockChain.get(i+1).getPreviousHash())) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

}

class DataLog {

    public DataLog(String pathname) {
        File file = new File("blockchain.txt");
        try {
            boolean createdNew = file.createNewFile();
            if (createdNew) {
                System.out.println("The file was successfully created.");
            } else {
                System.out.println("The file already exists.");
            }
        } catch (IOException e) {
            System.out.println("Cannot create the file: " + file.getPath());
        }
    }
}

class SerializationUtils {
    /**
     * Serialize the given object to the file
     */
    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}


public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        int blockChainLength = 5;
        BlockChain myBlockChain = null;

        File file = new File("blockchain.txt");

        /*if(file.exists()) {
            myBlockChain = (BlockChain)SerializationUtils.deserialize("blockchain.txt");
            //System.out.println("blockchain already exists");
        } else {
            try {
                file.createNewFile();
                myBlockChain = new BlockChain(blockChainLength);
            } catch (IOException e) {
                System.out.println("Cannot create the file: " + file.getPath());
            }
        }*/

        try {
            file.createNewFile();
            myBlockChain = new BlockChain(blockChainLength);
        } catch (IOException e) {
            System.out.println("Cannot create the file: " + file.getPath());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter how many zeros the hash must starts with: ");
        int proceedingNumberOfZeros = scanner.nextInt();
        System.out.println();

        for (int i = 0; i < 5; i++) {
            int start = LocalTime.now().toSecondOfDay();
            myBlockChain.addBlock(proceedingNumberOfZeros);
            int stop = LocalTime.now().toSecondOfDay();
            SerializationUtils.serialize(myBlockChain, "blockchain.txt");
            System.out.println(myBlockChain.getBlockChain().get(i).toString());
            System.out.println("Block was generating for " +  (stop - start) + " seconds");
            System.out.println();
        }

        /*System.out.println("===================================================================");
        BlockChain readBlockChain = (BlockChain)SerializationUtils.deserialize("blockchain.txt");
        System.out.println(myBlockChain.getBlockChain().get(0).toString());
*/


/*
        int proceedingNumberOfZeros = 2; // to be removed

        int blockChainLength = 5;
        BlockChain myBlockChain = new BlockChain(blockChainLength);

        for (int i = 0; i < 2; i++) {
            int start = LocalTime.now().toSecondOfDay();
            myBlockChain.addBlock(proceedingNumberOfZeros);
            int stop = LocalTime.now().toSecondOfDay();
            System.out.println(myBlockChain.getBlockChain().get(i).toString());
            System.out.println("Block was generating for " +  (stop - start) + " seconds");
            System.out.println();
        }

*/


        //print blockchain data
/*        if (myBlockChain.validateBlockChain()) {
            for (int i = 0; i < myBlockChain.getBlockChain().size(); i++) {
                System.out.println("Block:");
                System.out.println("Id: " + myBlockChain.getBlockChain().get(i).getId());
                System.out.println("Timestamp: " + myBlockChain.getBlockChain().get(i).getTimeStamp());
                System.out.println("Hash of the previous block:");
                System.out.println(myBlockChain.getBlockChain().get(i).getPreviousHash());
                System.out.println("Hash of the block:");
                System.out.println(myBlockChain.getBlockChain().get(i).getHash());
                System.out.println();
            }
        }*/
    }
}

