#include "BufferPool.h"

namespace smsbroker {

BufferPool::BufferSharedPtr BufferPool::get() {
	return BufferSharedPtr(m_bufferObjectPool.construct(),
			[&] (Buffer* pBuffer) {
				m_bufferObjectPool.destroy(pBuffer);
			});
}

}
