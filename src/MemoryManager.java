import javax.naming.InsufficientResourcesException;
import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private char[] buffer;
    private final int totalBytes;
    private int bytesFree;
    private List<MemoryBlock> freeMemoryBlocks;

    public MemoryManager(char[] buffer, int numBytes) {
        this.buffer = buffer;
        this.totalBytes = numBytes;
        this.bytesFree = numBytes;
        this.freeMemoryBlocks = new ArrayList<>();
        freeMemoryBlocks.add(new MemoryBlock(0, numBytes, null));
    }

    /**
     * @param size
     * @return
     * @throws InsufficientResourcesException
     */
    public MemoryBlock alloc(int size) throws InsufficientResourcesException {
        if (size > bytesFree) {
            throw new InsufficientResourcesException("Not enough space. Requested " + size + " bytes but only " + bytesFree + " are available");
        }
        MemoryBlock block = getFreeMemoryBlock(size);
        if (block != null) {
            this.bytesFree -= block.getSize();
        }
        return block;
    }

    /**
     * @param memToFree
     */
    public void free(MemoryBlock memToFree) {
        // TODO: Do we need to clear the memory? Probably
        while (memToFree != null) {
            MemoryBlock nextBlock = memToFree.getNextBlock();
            memToFree.setNextBlock(null);
            freeMemoryBlocks.add(memToFree);
            bytesFree += memToFree.getSize();

            memToFree = nextBlock;
        }
    }

    /**
     * @param block
     * @return
     */
    public String readBlock(MemoryBlock block) {
        int totalLength = block.getTotalLength();
        int readPosition = block.getStartingPosition();

        char[] data = new char[totalLength];
        for (int i = 0; i < totalLength; i++) {
            if (readPosition >= block.getEndPosition()) {
                block = block.getNextBlock();
                if (block == null) {
                    break;
                }
                readPosition = block.getStartingPosition();
            }
            data[i] = buffer[readPosition];
            readPosition++;
        }
        return new String(data);
    }

    /**
     * If I had more time with this method, I would implement a solution for when the input data is not
     * as long as the entire block. Could either add an ending character to indicate the end of the string,
     * or we could give the memory back to free memory
     *
     * @param block
     * @param data
     * @throws InsufficientResourcesException
     */
    public void writeToBlock(MemoryBlock block, String data) throws InsufficientResourcesException {
        if (block.getTotalLength() < data.length()) {
            throw new InsufficientResourcesException("Data is longer than the memory that is allocated");
        }
        int writePosition = block.getStartingPosition();
        for (int i = 0; i < data.length(); i++) {
            if (writePosition >= block.getEndPosition()) {
                // need to move to next fragmented block
                block = block.getNextBlock();
                writePosition = block.getStartingPosition();
            }
            buffer[writePosition] = data.charAt(i);
            writePosition++;
        }
    }

    public int getBytesFree() {
        return bytesFree;
    }

    private MemoryBlock getFreeMemoryBlock(int size) throws InsufficientResourcesException {
        if (freeMemoryBlocks.isEmpty()) {
            throw new InsufficientResourcesException("No free mem blocks");
        }
        MemoryBlock allocatedMemoryBlock = null;
        for (MemoryBlock memoryBlock : freeMemoryBlocks) {
            if (memoryBlock.getSize() >= size) {
                allocatedMemoryBlock = memoryBlock;
                break;
            }
        }

        if (allocatedMemoryBlock != null) {
            if (allocatedMemoryBlock.getSize() == size) {
                freeMemoryBlocks.remove(allocatedMemoryBlock);
                return allocatedMemoryBlock;
            } else if (allocatedMemoryBlock.getSize() > size) {
                return splitMemoryBlock(allocatedMemoryBlock, size);
            }
        }
        return getFragmentedMemoryBlock(size);
    }

    private MemoryBlock splitMemoryBlock(MemoryBlock allocatedMemoryBlock, int bytesNeeded) {
        MemoryBlock newAllocatedMemoryBlock = new MemoryBlock(allocatedMemoryBlock.getStartingPosition(), bytesNeeded, null);
        MemoryBlock newFreeBlock = new MemoryBlock(
                allocatedMemoryBlock.getStartingPosition() + bytesNeeded,
                allocatedMemoryBlock.getSize() - bytesNeeded,
                null);
        freeMemoryBlocks.remove(allocatedMemoryBlock);
        freeMemoryBlocks.add(newFreeBlock);

        return newAllocatedMemoryBlock;
    }

    private MemoryBlock getFragmentedMemoryBlock(int bytesNeeded) {
        MemoryBlock fragmentedBlockHead = freeMemoryBlocks.get(0);
        freeMemoryBlocks.remove(fragmentedBlockHead);
        bytesNeeded -= fragmentedBlockHead.getSize();

        MemoryBlock lastBlock = fragmentedBlockHead;
        while (bytesNeeded > 0) {
            if (bytesNeeded == freeMemoryBlocks.get(0).getSize()) {
                lastBlock.setNextBlock(freeMemoryBlocks.get(0));
                bytesNeeded = 0;
            } else if (bytesNeeded < freeMemoryBlocks.get(0).getSize()) {
                lastBlock.setNextBlock(splitMemoryBlock(freeMemoryBlocks.get(0), bytesNeeded));
                bytesNeeded = 0;
            }
            MemoryBlock newBlock = freeMemoryBlocks.get(0);
            freeMemoryBlocks.remove(newBlock);
            lastBlock.setNextBlock(newBlock);
            lastBlock = newBlock;
            bytesNeeded -= newBlock.getSize();
        }
        return fragmentedBlockHead;
    }

    public void printFreeMemory() {
        if (freeMemoryBlocks.isEmpty()) {
            System.out.println("No free memory blocks");
        }
        for (MemoryBlock memoryBlock : freeMemoryBlocks) {
            System.out.println(memoryBlock);
        }
    }

    public void printBuffer() {
        String bufferOutput = new String(buffer);
        System.out.println("Buffer: " + bufferOutput);
    }
}
