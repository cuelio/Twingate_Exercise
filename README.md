## Implementation Details
The MemoryManager supports alloc, free, read, and write. When a MemoryManager object is created, 
it is given a char array (named buffer), this is the total use-able memory for the MemoryManager. In order to keep 
track of the allocated blocks of memory, I decided to create a MemoryBlock class that was a logical layer on top of the 
buffer. It can represent both free and allocated memory, and keeps track of the starting position in the buffer and how 
many bytes are allocated for that block.

In order to handle allocation requests, the MemoryManager maintains a list of free memory blocks, and the number of 
bytes that are currently free across the entire buffer. In general, the alloc method is done like so:
1. Check to see if there are enough bytes available across the entire buffer. If there are, we know we can satisfy the
   request, even if it is not a contiguous memory block. 
2. First search for a free memory block that is >= the requested size. We would prefer to not fragment if possible.
   1. If we find one that is exactly as big as the requested size, return the block and remove it from the free blocks list
   2. If it is larger than the requested size, we split it into two blocks. One is returned to the requester as allocated
      memory, and the other is leftover memory that is put into the free blocks list.
3. If we can't find a block that is >= the requested size, we need to return a fragmented block. I implemented fragmentation
   by basically making a MemoryBlock a linked list node, where I can chain together blocks by setting MemoryBlock.nextBlock
   1. The head of the fragmented MemoryBlock list is created as the first element in the free memory blocks list, and then
      we iterate until we have satisfied the total size requested. If the last block is not a perfect fit, we will split that one.

For freeing memory, we just start at the first MemoryBlock that is passed in and traverse the linked list of MemoryBlocks,
freeing each one and adding back to our bytesFree count in the memory manger.

Reads and writes start at a block, and we set a variable read/write position that is the starting point in the buffer of
that MemoryBlock. We increment the read/write position until we reach the end of the block. Then we either advance to the
next block if it is fragmented or we stop reading/writing there is no MemoryBlock.nextBlock. Each time we advance to a new
block we need to reset the read/write position in the buffer to the new block's starting position. 

#### Improvements I would make
1. Reduce fragmentation, a few approaches:
   1. Merge free MemoryBlocks in the free method. If two free memory blocks are adjacent we should merge them into one.
      This will give us larger blocks in general and reduce the likelihood we need to fragment during alloc.
   2. Another variation of #1 is to occasionally clean up and merge the free memory blocks. For example, the size of the 
      free memory blocks list passes a certain threshold, we go through the list and perform any merges possible.
   3. Optimize block size during alloc, i.e. find a best size fit for the request. I started trying to implement this but 
      didn't think I had time to finish the implementation. Two approaches:
      1. Greedy approach - always take the largest free block and split it if necessary. Implement using a max-heap based
         on block size
      2. Best fit - Could use something like a binary search tree to store the free memory blocks, and find the closest fit
2. Add block ownership/permissions, so only the owner of the block can perform the read/write/free operations.
3. Indexing in a fragmented block. Haven't thought much about a solution for this, but one downside of the linked list
   approach is that you may need to traverse the entire linked list to find a specific index. One could implement a look-up
   table of sorts that translates a logical memory block index to an actually buffer index quickly. 

#### Minor things I would improve with more time
1. Complete Javadoc comments for public methods
2. Increase test coverage