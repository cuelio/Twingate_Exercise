import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InsufficientResourcesException;
import java.util.ArrayList;
import java.util.List;

public class MemoryManagerTest {
    private static final int MEM_SIZE = 10;
    private MemoryManager memoryManager;

    @Before
    public void setup() {
        memoryManager = new MemoryManager(new char[MEM_SIZE], MEM_SIZE);
    }

    @Test (expected = InsufficientResourcesException.class)
    public void testAlloc_whenRequestedBytesIsMoreThanIsAvailable_InsufficientResourcesException() throws InsufficientResourcesException {
        Assert.assertEquals(MEM_SIZE, memoryManager.getBytesFree());
        memoryManager.alloc(MEM_SIZE + 1);
    }

    @Test
    public void testAlloc_allocateEntireBufferContiguously() throws InsufficientResourcesException {
        MemoryBlock allocatedBlock = memoryManager.alloc(MEM_SIZE);

        Assert.assertEquals(0, memoryManager.getBytesFree());
        Assert.assertNotNull(allocatedBlock);
        Assert.assertNull(allocatedBlock.getNextBlock());
        Assert.assertEquals(MEM_SIZE, allocatedBlock.getSize());
        Assert.assertEquals(MEM_SIZE, allocatedBlock.getTotalLength());
        Assert.assertEquals(0, allocatedBlock.getStartingPosition());
        Assert.assertEquals(MEM_SIZE, allocatedBlock.getEndPosition());
    }

    @Test
    public void testAlloc_allocateEntireBufferIndividually() throws InsufficientResourcesException {
        List<MemoryBlock> memoryBlocks = new ArrayList<>();
        for (int i = 0; i < MEM_SIZE; i++) {
            memoryBlocks.add(memoryManager.alloc(1));
        }
        Assert.assertEquals(MEM_SIZE, memoryBlocks.size());
        Assert.assertEquals(0, memoryManager.getBytesFree());

        for (int i = 0; i < MEM_SIZE; i++) {
            MemoryBlock memBlock = memoryBlocks.get(i);

            Assert.assertEquals(1, memBlock.getSize());
            Assert.assertEquals(1, memBlock.getTotalLength());
            Assert.assertEquals(i, memBlock.getStartingPosition());
            Assert.assertEquals(i+1, memBlock.getEndPosition());
            Assert.assertNull(memBlock.getNextBlock());
        }
    }

    @Test
    public void testAlloc_allocateEntireBufferFragmented() throws InsufficientResourcesException {
        fragmentMemoryEntirely();

        // At this point our list of free blocks is of size MEM_SIZE, each with 1 byte
        // If we ask for MEM_SIZE bytes, we should get MEM_SIZE different fragmented bytes
        MemoryBlock fragmentedBlockHead = memoryManager.alloc(10);

        Assert.assertNotNull(fragmentedBlockHead);
        Assert.assertEquals(MEM_SIZE, fragmentedBlockHead.getTotalLength());

        MemoryBlock currentBlock = fragmentedBlockHead;
        for (int i = 0; i < MEM_SIZE; i++) {
            Assert.assertNotNull(currentBlock);
            Assert.assertEquals(1, currentBlock.getSize());
            Assert.assertEquals(i, currentBlock.getStartingPosition());
            Assert.assertEquals(i + 1, currentBlock.getEndPosition());

            if (i == MEM_SIZE - 1) {
                Assert.assertNull(currentBlock.getNextBlock());
            } else {
                Assert.assertNotNull(currentBlock.getNextBlock());
                currentBlock = currentBlock.getNextBlock();
            }
        }
    }

    @Test
    public void testReadAndWrite_withEntireBufferAllocatedContiguously() throws InsufficientResourcesException {
        MemoryBlock allocatedBlock = memoryManager.alloc(MEM_SIZE);

        String stringToWrite = "Twingate10";
        memoryManager.writeToBlock(allocatedBlock, stringToWrite);

        String stringFromRead = memoryManager.readBlock(allocatedBlock);
        Assert.assertEquals(stringToWrite, stringFromRead);
    }

    @Test
    public void testReadAndWrite_withEntireBufferAllocatedFragmented() throws InsufficientResourcesException {
        fragmentMemoryEntirely();
        MemoryBlock allocatedBlock = memoryManager.alloc(MEM_SIZE);

        String stringToWrite = "Twingate10";
        memoryManager.writeToBlock(allocatedBlock, stringToWrite);

        String stringFromRead = memoryManager.readBlock(allocatedBlock);
        Assert.assertEquals(stringToWrite, stringFromRead);
    }

    @Test
    public void testReadAndWrite_withPartiallyFragmentedBuffer() throws InsufficientResourcesException {
        MemoryBlock[] blocks = new MemoryBlock[MEM_SIZE];
        for (int i = 0; i < MEM_SIZE; i++) {
            blocks[i] = memoryManager.alloc(1);
        }
        memoryManager.free(blocks[2]);
        memoryManager.free(blocks[5]);
        memoryManager.free(blocks[7]);

        MemoryBlock fragmentedBlock = memoryManager.alloc(3);
        Assert.assertNotNull(fragmentedBlock);
        Assert.assertEquals(3, fragmentedBlock.getTotalLength());

        memoryManager.writeToBlock(fragmentedBlock, "123");

        String resultFromRead = memoryManager.readBlock(fragmentedBlock);
        Assert.assertEquals("123", resultFromRead);
    }

    @Test
    public void testFree_whenTwoBlocksAreAllocatedAndFreed() throws InsufficientResourcesException {
        Assert.assertEquals(MEM_SIZE, memoryManager.getBytesFree());
        MemoryBlock block1 = memoryManager.alloc(MEM_SIZE/2);
        MemoryBlock block2 = memoryManager.alloc(MEM_SIZE/2);
        Assert.assertEquals(0, memoryManager.getBytesFree());
        memoryManager.free(block1);
        Assert.assertEquals(MEM_SIZE/2, memoryManager.getBytesFree());
        memoryManager.free(block1);
        Assert.assertEquals(MEM_SIZE, memoryManager.getBytesFree());

    }

    private void fragmentMemoryEntirely() throws InsufficientResourcesException {
        MemoryBlock[] blocks = new MemoryBlock[MEM_SIZE];
        for (int i = 0; i < MEM_SIZE; i++) {
            blocks[i] = memoryManager.alloc(1);
        }
        for (int i = 0; i < MEM_SIZE; i++) {
            memoryManager.free(blocks[i]);
        }
    }
}
