### Program Functionality

This program is a memory manager that supports the four following operations:
1. Alloc - Takes in an input of the number of bytes requested and returns a MemoryBlock object
    if the allocation was successful
2. Free - Takes in a MemoryBlock object and frees the memory assocaited with it
3. Write
4. Read

### Implementation Details

I decided to create a MemoryBlock class that was a logical layer on top of the buffer. 
It can represent both free and allocated memory, and keeps track of the starting position in the buffer

, it keeps
track of the starting position of the block in the actual buffer

#### Improvements I would make with more time