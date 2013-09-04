#include "BufferPool.h"
#include "Log.h"

namespace smsbroker {

BufferPool::BufferPool() :
		m_size(128) {
	m_bufferVector.reserve(m_size);
	for (size_t i = 0; i < m_size; ++i) {
		m_bufferVector.push_back(new std::vector<unsigned char>);
	}
}

BufferPool::BufferSharedPtr BufferPool::get() {
	if (m_bufferVector.empty()) {
		m_bufferVector.reserve(m_size * 2);
		for (size_t i = 0; i < m_size; ++i) {
			m_bufferVector.push_back(new std::vector<unsigned char>);
		}
		m_size *= 2;

		Log::getInfoInstance() << "buffer pool size " << m_size;
	}

	BufferSharedPtr retVal(m_bufferVector.back(), [&] (Buffer* pBuffer) {
		m_bufferVector.push_back(pBuffer);
	});
	m_bufferVector.pop_back();
	return retVal;
}

}
