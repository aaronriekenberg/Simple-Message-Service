#ifndef BUFFER_POOL_H
#define BUFFER_POOL_H

#include <memory>
#include <vector>

namespace smsbroker {

class BufferPool {
public:
	typedef std::vector<unsigned char> Buffer;

	typedef std::shared_ptr<Buffer> BufferSharedPtr;

	BufferPool();

	~BufferPool() = default;

	BufferSharedPtr get();

private:
	BufferPool(const BufferPool& rhs) = delete;

	BufferPool& operator=(const BufferPool& rhs) = delete;

	std::vector<Buffer*> m_bufferVector;

	size_t m_size;

};

}

#endif
