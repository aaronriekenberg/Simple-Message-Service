#ifndef BUFFER_H
#define BUFFER_H

#include <memory>
#include <vector>

namespace smsbroker {

typedef std::vector<unsigned char> Buffer;

typedef std::shared_ptr<Buffer> BufferSharedPtr;

typedef std::shared_ptr<const Buffer> ConstBufferSharedPtr;

}

#endif
