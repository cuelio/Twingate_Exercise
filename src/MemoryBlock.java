public class MemoryBlock {
    private final int startingPosition;
    private final int numBytes;
    private MemoryBlock nextBlock;

    public MemoryBlock(int startingPosition, int numBytes, MemoryBlock nextBlock) {
        this.startingPosition = startingPosition;
        this.numBytes = numBytes;
        this.nextBlock = nextBlock;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    public int getEndPosition() {
        return startingPosition + numBytes;
    }

    public int getSize() {
        return numBytes;
    }

    public void setNextBlock(MemoryBlock block) {
        nextBlock = block;
    }

    public MemoryBlock getNextBlock() {
        return nextBlock;
    }

    /**
     * This method is intented to return the total size of a chain of fragmented blocks
     * e.g. if we had two fragmented blocks, this block is of size 3 and nextBlock is of size 5 this would return 8
     * @return total length of this block plus all the blocks accessed through this.nextBlock
     */
    public int getTotalLength() {
        int length = 0;
        MemoryBlock nextBlock = this;
        while (nextBlock != null) {
            length += nextBlock.getSize();
            nextBlock = nextBlock.getNextBlock();
        }
        return length;
    }

    public String toString() {
        return "MemoryBlock. Start: " + getStartingPosition() + ", end: "
                + getEndPosition() + ", size: " + getSize();
    }

    public void printFragmentedBlock() {
        System.out.println("Printing fragmented block");
        MemoryBlock nextBlock = this;
        while (nextBlock != null) {
            System.out.println(nextBlock);
            nextBlock = nextBlock.getNextBlock();
        }
    }
}

// documentation
// 1. description of algorithm / implemetnation (tech spec)
// 2. What would I do if I had more time
