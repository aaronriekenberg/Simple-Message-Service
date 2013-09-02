#ifndef BUFFER_POOL_H
#define BUFFER_POOL_H

#include <boost/pool/object_pool.hpp>
#include <memory>
#include <vector>

namespace smsbroker {

class BufferPool {
public:
	typedef std::vector<unsigned char> Buffer;

	typedef std::shared_ptr<Buffer> BufferSharedPtr;

	BufferPool() = default;

	~BufferPool() = default;

	BufferSharedPtr get();

private:
	BufferPool(const BufferPool& rhs) = delete;

	BufferPool& operator=(const BufferPool& rhs) = delete;

	boost::object_pool<Buffer> m_bufferObjectPool;

};

}

#endif
