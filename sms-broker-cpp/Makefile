CXX = g++
CXXFLAGS = -O3 -std=gnu++11 -g -O3 -Wall -pthread
LDFLAGS = -pthread -lprotobuf -lboost_system
SRC = BoostSystemUtil.cpp \
      Broker.cpp \
      ClientAcceptor.cpp \
      ClientSession.cpp \
      Log.cpp \
      Main.cpp \
      SMSProtocol.pb.cpp \
      TcpResolver.cpp \
      ThreadName.cpp \
      Topic.cpp \
      TopicContainer.cpp
OBJS = ${SRC:.cpp=.o}

all: cppsmsbroker

clean:
	rm -f *.o cppsmsbroker

cppsmsbroker: $(OBJS)
	$(CXX) $(OBJS) $(LDFLAGS) -o $@

depend:
	$(CXX) $(CXXFLAGS) -MM $(SRC) > .makeinclude

include .makeinclude
