// Generated by the protocol buffer compiler.  DO NOT EDIT!

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "SMSProtocol.pb.h"

#include <algorithm>

#include <google/protobuf/stubs/once.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/wire_format_lite_inl.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/reflection_ops.h>
#include <google/protobuf/wire_format.h>
// @@protoc_insertion_point(includes)

namespace sms {
namespace protocol {
namespace protobuf {

namespace {

const ::google::protobuf::Descriptor* ClientToBrokerMessage_descriptor_ = NULL;
const ::google::protobuf::internal::GeneratedMessageReflection*
  ClientToBrokerMessage_reflection_ = NULL;
const ::google::protobuf::EnumDescriptor* ClientToBrokerMessage_ClientToBrokerMessageType_descriptor_ = NULL;
const ::google::protobuf::Descriptor* BrokerToClientMessage_descriptor_ = NULL;
const ::google::protobuf::internal::GeneratedMessageReflection*
  BrokerToClientMessage_reflection_ = NULL;
const ::google::protobuf::EnumDescriptor* BrokerToClientMessage_BrokerToClientMessageType_descriptor_ = NULL;

}  // namespace


void protobuf_AssignDesc_SMSProtocol_2eproto() {
  protobuf_AddDesc_SMSProtocol_2eproto();
  const ::google::protobuf::FileDescriptor* file =
    ::google::protobuf::DescriptorPool::generated_pool()->FindFileByName(
      "SMSProtocol.proto");
  GOOGLE_CHECK(file != NULL);
  ClientToBrokerMessage_descriptor_ = file->message_type(0);
  static const int ClientToBrokerMessage_offsets_[3] = {
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(ClientToBrokerMessage, messagetype_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(ClientToBrokerMessage, topicname_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(ClientToBrokerMessage, messagepayload_),
  };
  ClientToBrokerMessage_reflection_ =
    new ::google::protobuf::internal::GeneratedMessageReflection(
      ClientToBrokerMessage_descriptor_,
      ClientToBrokerMessage::default_instance_,
      ClientToBrokerMessage_offsets_,
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(ClientToBrokerMessage, _has_bits_[0]),
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(ClientToBrokerMessage, _unknown_fields_),
      -1,
      ::google::protobuf::DescriptorPool::generated_pool(),
      ::google::protobuf::MessageFactory::generated_factory(),
      sizeof(ClientToBrokerMessage));
  ClientToBrokerMessage_ClientToBrokerMessageType_descriptor_ = ClientToBrokerMessage_descriptor_->enum_type(0);
  BrokerToClientMessage_descriptor_ = file->message_type(1);
  static const int BrokerToClientMessage_offsets_[3] = {
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(BrokerToClientMessage, messagetype_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(BrokerToClientMessage, topicname_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(BrokerToClientMessage, messagepayload_),
  };
  BrokerToClientMessage_reflection_ =
    new ::google::protobuf::internal::GeneratedMessageReflection(
      BrokerToClientMessage_descriptor_,
      BrokerToClientMessage::default_instance_,
      BrokerToClientMessage_offsets_,
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(BrokerToClientMessage, _has_bits_[0]),
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(BrokerToClientMessage, _unknown_fields_),
      -1,
      ::google::protobuf::DescriptorPool::generated_pool(),
      ::google::protobuf::MessageFactory::generated_factory(),
      sizeof(BrokerToClientMessage));
  BrokerToClientMessage_BrokerToClientMessageType_descriptor_ = BrokerToClientMessage_descriptor_->enum_type(0);
}

namespace {

GOOGLE_PROTOBUF_DECLARE_ONCE(protobuf_AssignDescriptors_once_);
inline void protobuf_AssignDescriptorsOnce() {
  ::google::protobuf::GoogleOnceInit(&protobuf_AssignDescriptors_once_,
                 &protobuf_AssignDesc_SMSProtocol_2eproto);
}

void protobuf_RegisterTypes(const ::std::string&) {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedMessage(
    ClientToBrokerMessage_descriptor_, &ClientToBrokerMessage::default_instance());
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedMessage(
    BrokerToClientMessage_descriptor_, &BrokerToClientMessage::default_instance());
}

}  // namespace

void protobuf_ShutdownFile_SMSProtocol_2eproto() {
  delete ClientToBrokerMessage::default_instance_;
  delete ClientToBrokerMessage_reflection_;
  delete BrokerToClientMessage::default_instance_;
  delete BrokerToClientMessage_reflection_;
}

void protobuf_AddDesc_SMSProtocol_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  ::google::protobuf::DescriptorPool::InternalAddGeneratedFile(
    "\n\021SMSProtocol.proto\022\025sms.protocol.protob"
    "uf\"\240\002\n\025ClientToBrokerMessage\022[\n\013messageT"
    "ype\030\001 \002(\0162F.sms.protocol.protobuf.Client"
    "ToBrokerMessage.ClientToBrokerMessageTyp"
    "e\022\021\n\ttopicName\030\002 \002(\t\022\026\n\016messagePayload\030\003"
    " \001(\014\"\177\n\031ClientToBrokerMessageType\022\035\n\031CLI"
    "ENT_SUBSCRIBE_TO_TOPIC\020\000\022!\n\035CLIENT_UNSUB"
    "SCRIBE_FROM_TOPIC\020\001\022 \n\034CLIENT_SEND_MESSA"
    "GE_TO_TOPIC\020\002\"\336\001\n\025BrokerToClientMessage\022"
    "[\n\013messageType\030\001 \002(\0162F.sms.protocol.prot"
    "obuf.BrokerToClientMessage.BrokerToClien"
    "tMessageType\022\021\n\ttopicName\030\002 \002(\t\022\026\n\016messa"
    "gePayload\030\003 \001(\014\"=\n\031BrokerToClientMessage"
    "Type\022 \n\034BROKER_TOPIC_MESSAGE_PUBLISH\020\000B."
    "\n\037org.aaron.sms.protocol.protobufB\013SMSPr"
    "otocol", 606);
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedFile(
    "SMSProtocol.proto", &protobuf_RegisterTypes);
  ClientToBrokerMessage::default_instance_ = new ClientToBrokerMessage();
  BrokerToClientMessage::default_instance_ = new BrokerToClientMessage();
  ClientToBrokerMessage::default_instance_->InitAsDefaultInstance();
  BrokerToClientMessage::default_instance_->InitAsDefaultInstance();
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_SMSProtocol_2eproto);
}

// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_SMSProtocol_2eproto {
  StaticDescriptorInitializer_SMSProtocol_2eproto() {
    protobuf_AddDesc_SMSProtocol_2eproto();
  }
} static_descriptor_initializer_SMSProtocol_2eproto_;


// ===================================================================

const ::google::protobuf::EnumDescriptor* ClientToBrokerMessage_ClientToBrokerMessageType_descriptor() {
  protobuf_AssignDescriptorsOnce();
  return ClientToBrokerMessage_ClientToBrokerMessageType_descriptor_;
}
bool ClientToBrokerMessage_ClientToBrokerMessageType_IsValid(int value) {
  switch(value) {
    case 0:
    case 1:
    case 2:
      return true;
    default:
      return false;
  }
}

#ifndef _MSC_VER
const ClientToBrokerMessage_ClientToBrokerMessageType ClientToBrokerMessage::CLIENT_SUBSCRIBE_TO_TOPIC;
const ClientToBrokerMessage_ClientToBrokerMessageType ClientToBrokerMessage::CLIENT_UNSUBSCRIBE_FROM_TOPIC;
const ClientToBrokerMessage_ClientToBrokerMessageType ClientToBrokerMessage::CLIENT_SEND_MESSAGE_TO_TOPIC;
const ClientToBrokerMessage_ClientToBrokerMessageType ClientToBrokerMessage::ClientToBrokerMessageType_MIN;
const ClientToBrokerMessage_ClientToBrokerMessageType ClientToBrokerMessage::ClientToBrokerMessageType_MAX;
const int ClientToBrokerMessage::ClientToBrokerMessageType_ARRAYSIZE;
#endif  // _MSC_VER
#ifndef _MSC_VER
const int ClientToBrokerMessage::kMessageTypeFieldNumber;
const int ClientToBrokerMessage::kTopicNameFieldNumber;
const int ClientToBrokerMessage::kMessagePayloadFieldNumber;
#endif  // !_MSC_VER

ClientToBrokerMessage::ClientToBrokerMessage()
  : ::google::protobuf::Message() {
  SharedCtor();
}

void ClientToBrokerMessage::InitAsDefaultInstance() {
}

ClientToBrokerMessage::ClientToBrokerMessage(const ClientToBrokerMessage& from)
  : ::google::protobuf::Message() {
  SharedCtor();
  MergeFrom(from);
}

void ClientToBrokerMessage::SharedCtor() {
  _cached_size_ = 0;
  messagetype_ = 0;
  topicname_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  messagepayload_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

ClientToBrokerMessage::~ClientToBrokerMessage() {
  SharedDtor();
}

void ClientToBrokerMessage::SharedDtor() {
  if (topicname_ != &::google::protobuf::internal::kEmptyString) {
    delete topicname_;
  }
  if (messagepayload_ != &::google::protobuf::internal::kEmptyString) {
    delete messagepayload_;
  }
  if (this != default_instance_) {
  }
}

void ClientToBrokerMessage::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const ::google::protobuf::Descriptor* ClientToBrokerMessage::descriptor() {
  protobuf_AssignDescriptorsOnce();
  return ClientToBrokerMessage_descriptor_;
}

const ClientToBrokerMessage& ClientToBrokerMessage::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_SMSProtocol_2eproto();  return *default_instance_;
}

ClientToBrokerMessage* ClientToBrokerMessage::default_instance_ = NULL;

ClientToBrokerMessage* ClientToBrokerMessage::New() const {
  return new ClientToBrokerMessage;
}

void ClientToBrokerMessage::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    messagetype_ = 0;
    if (has_topicname()) {
      if (topicname_ != &::google::protobuf::internal::kEmptyString) {
        topicname_->clear();
      }
    }
    if (has_messagepayload()) {
      if (messagepayload_ != &::google::protobuf::internal::kEmptyString) {
        messagepayload_->clear();
      }
    }
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
  mutable_unknown_fields()->Clear();
}

bool ClientToBrokerMessage::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required .sms.protocol.protobuf.ClientToBrokerMessage.ClientToBrokerMessageType messageType = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
          int value;
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   int, ::google::protobuf::internal::WireFormatLite::TYPE_ENUM>(
                 input, &value)));
          if (::sms::protocol::protobuf::ClientToBrokerMessage_ClientToBrokerMessageType_IsValid(value)) {
            set_messagetype(static_cast< ::sms::protocol::protobuf::ClientToBrokerMessage_ClientToBrokerMessageType >(value));
          } else {
            mutable_unknown_fields()->AddVarint(1, value);
          }
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_topicName;
        break;
      }
      
      // required string topicName = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_topicName:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_topicname()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->topicname().data(), this->topicname().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(26)) goto parse_messagePayload;
        break;
      }
      
      // optional bytes messagePayload = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_messagePayload:
          DO_(::google::protobuf::internal::WireFormatLite::ReadBytes(
                input, this->mutable_messagepayload()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }
      
      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormat::SkipField(
              input, tag, mutable_unknown_fields()));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void ClientToBrokerMessage::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required .sms.protocol.protobuf.ClientToBrokerMessage.ClientToBrokerMessageType messageType = 1;
  if (has_messagetype()) {
    ::google::protobuf::internal::WireFormatLite::WriteEnum(
      1, this->messagetype(), output);
  }
  
  // required string topicName = 2;
  if (has_topicname()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->topicname().data(), this->topicname().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->topicname(), output);
  }
  
  // optional bytes messagePayload = 3;
  if (has_messagepayload()) {
    ::google::protobuf::internal::WireFormatLite::WriteBytes(
      3, this->messagepayload(), output);
  }
  
  if (!unknown_fields().empty()) {
    ::google::protobuf::internal::WireFormat::SerializeUnknownFields(
        unknown_fields(), output);
  }
}

::google::protobuf::uint8* ClientToBrokerMessage::SerializeWithCachedSizesToArray(
    ::google::protobuf::uint8* target) const {
  // required .sms.protocol.protobuf.ClientToBrokerMessage.ClientToBrokerMessageType messageType = 1;
  if (has_messagetype()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteEnumToArray(
      1, this->messagetype(), target);
  }
  
  // required string topicName = 2;
  if (has_topicname()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->topicname().data(), this->topicname().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        2, this->topicname(), target);
  }
  
  // optional bytes messagePayload = 3;
  if (has_messagepayload()) {
    target =
      ::google::protobuf::internal::WireFormatLite::WriteBytesToArray(
        3, this->messagepayload(), target);
  }
  
  if (!unknown_fields().empty()) {
    target = ::google::protobuf::internal::WireFormat::SerializeUnknownFieldsToArray(
        unknown_fields(), target);
  }
  return target;
}

int ClientToBrokerMessage::ByteSize() const {
  int total_size = 0;
  
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required .sms.protocol.protobuf.ClientToBrokerMessage.ClientToBrokerMessageType messageType = 1;
    if (has_messagetype()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::EnumSize(this->messagetype());
    }
    
    // required string topicName = 2;
    if (has_topicname()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->topicname());
    }
    
    // optional bytes messagePayload = 3;
    if (has_messagepayload()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::BytesSize(
          this->messagepayload());
    }
    
  }
  if (!unknown_fields().empty()) {
    total_size +=
      ::google::protobuf::internal::WireFormat::ComputeUnknownFieldsSize(
        unknown_fields());
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void ClientToBrokerMessage::MergeFrom(const ::google::protobuf::Message& from) {
  GOOGLE_CHECK_NE(&from, this);
  const ClientToBrokerMessage* source =
    ::google::protobuf::internal::dynamic_cast_if_available<const ClientToBrokerMessage*>(
      &from);
  if (source == NULL) {
    ::google::protobuf::internal::ReflectionOps::Merge(from, this);
  } else {
    MergeFrom(*source);
  }
}

void ClientToBrokerMessage::MergeFrom(const ClientToBrokerMessage& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_messagetype()) {
      set_messagetype(from.messagetype());
    }
    if (from.has_topicname()) {
      set_topicname(from.topicname());
    }
    if (from.has_messagepayload()) {
      set_messagepayload(from.messagepayload());
    }
  }
  mutable_unknown_fields()->MergeFrom(from.unknown_fields());
}

void ClientToBrokerMessage::CopyFrom(const ::google::protobuf::Message& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

void ClientToBrokerMessage::CopyFrom(const ClientToBrokerMessage& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool ClientToBrokerMessage::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000003) != 0x00000003) return false;
  
  return true;
}

void ClientToBrokerMessage::Swap(ClientToBrokerMessage* other) {
  if (other != this) {
    std::swap(messagetype_, other->messagetype_);
    std::swap(topicname_, other->topicname_);
    std::swap(messagepayload_, other->messagepayload_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    _unknown_fields_.Swap(&other->_unknown_fields_);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::google::protobuf::Metadata ClientToBrokerMessage::GetMetadata() const {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::Metadata metadata;
  metadata.descriptor = ClientToBrokerMessage_descriptor_;
  metadata.reflection = ClientToBrokerMessage_reflection_;
  return metadata;
}


// ===================================================================

const ::google::protobuf::EnumDescriptor* BrokerToClientMessage_BrokerToClientMessageType_descriptor() {
  protobuf_AssignDescriptorsOnce();
  return BrokerToClientMessage_BrokerToClientMessageType_descriptor_;
}
bool BrokerToClientMessage_BrokerToClientMessageType_IsValid(int value) {
  switch(value) {
    case 0:
      return true;
    default:
      return false;
  }
}

#ifndef _MSC_VER
const BrokerToClientMessage_BrokerToClientMessageType BrokerToClientMessage::BROKER_TOPIC_MESSAGE_PUBLISH;
const BrokerToClientMessage_BrokerToClientMessageType BrokerToClientMessage::BrokerToClientMessageType_MIN;
const BrokerToClientMessage_BrokerToClientMessageType BrokerToClientMessage::BrokerToClientMessageType_MAX;
const int BrokerToClientMessage::BrokerToClientMessageType_ARRAYSIZE;
#endif  // _MSC_VER
#ifndef _MSC_VER
const int BrokerToClientMessage::kMessageTypeFieldNumber;
const int BrokerToClientMessage::kTopicNameFieldNumber;
const int BrokerToClientMessage::kMessagePayloadFieldNumber;
#endif  // !_MSC_VER

BrokerToClientMessage::BrokerToClientMessage()
  : ::google::protobuf::Message() {
  SharedCtor();
}

void BrokerToClientMessage::InitAsDefaultInstance() {
}

BrokerToClientMessage::BrokerToClientMessage(const BrokerToClientMessage& from)
  : ::google::protobuf::Message() {
  SharedCtor();
  MergeFrom(from);
}

void BrokerToClientMessage::SharedCtor() {
  _cached_size_ = 0;
  messagetype_ = 0;
  topicname_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  messagepayload_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

BrokerToClientMessage::~BrokerToClientMessage() {
  SharedDtor();
}

void BrokerToClientMessage::SharedDtor() {
  if (topicname_ != &::google::protobuf::internal::kEmptyString) {
    delete topicname_;
  }
  if (messagepayload_ != &::google::protobuf::internal::kEmptyString) {
    delete messagepayload_;
  }
  if (this != default_instance_) {
  }
}

void BrokerToClientMessage::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const ::google::protobuf::Descriptor* BrokerToClientMessage::descriptor() {
  protobuf_AssignDescriptorsOnce();
  return BrokerToClientMessage_descriptor_;
}

const BrokerToClientMessage& BrokerToClientMessage::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_SMSProtocol_2eproto();  return *default_instance_;
}

BrokerToClientMessage* BrokerToClientMessage::default_instance_ = NULL;

BrokerToClientMessage* BrokerToClientMessage::New() const {
  return new BrokerToClientMessage;
}

void BrokerToClientMessage::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    messagetype_ = 0;
    if (has_topicname()) {
      if (topicname_ != &::google::protobuf::internal::kEmptyString) {
        topicname_->clear();
      }
    }
    if (has_messagepayload()) {
      if (messagepayload_ != &::google::protobuf::internal::kEmptyString) {
        messagepayload_->clear();
      }
    }
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
  mutable_unknown_fields()->Clear();
}

bool BrokerToClientMessage::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required .sms.protocol.protobuf.BrokerToClientMessage.BrokerToClientMessageType messageType = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
          int value;
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   int, ::google::protobuf::internal::WireFormatLite::TYPE_ENUM>(
                 input, &value)));
          if (::sms::protocol::protobuf::BrokerToClientMessage_BrokerToClientMessageType_IsValid(value)) {
            set_messagetype(static_cast< ::sms::protocol::protobuf::BrokerToClientMessage_BrokerToClientMessageType >(value));
          } else {
            mutable_unknown_fields()->AddVarint(1, value);
          }
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_topicName;
        break;
      }
      
      // required string topicName = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_topicName:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_topicname()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->topicname().data(), this->topicname().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(26)) goto parse_messagePayload;
        break;
      }
      
      // optional bytes messagePayload = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_messagePayload:
          DO_(::google::protobuf::internal::WireFormatLite::ReadBytes(
                input, this->mutable_messagepayload()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }
      
      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormat::SkipField(
              input, tag, mutable_unknown_fields()));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void BrokerToClientMessage::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required .sms.protocol.protobuf.BrokerToClientMessage.BrokerToClientMessageType messageType = 1;
  if (has_messagetype()) {
    ::google::protobuf::internal::WireFormatLite::WriteEnum(
      1, this->messagetype(), output);
  }
  
  // required string topicName = 2;
  if (has_topicname()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->topicname().data(), this->topicname().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->topicname(), output);
  }
  
  // optional bytes messagePayload = 3;
  if (has_messagepayload()) {
    ::google::protobuf::internal::WireFormatLite::WriteBytes(
      3, this->messagepayload(), output);
  }
  
  if (!unknown_fields().empty()) {
    ::google::protobuf::internal::WireFormat::SerializeUnknownFields(
        unknown_fields(), output);
  }
}

::google::protobuf::uint8* BrokerToClientMessage::SerializeWithCachedSizesToArray(
    ::google::protobuf::uint8* target) const {
  // required .sms.protocol.protobuf.BrokerToClientMessage.BrokerToClientMessageType messageType = 1;
  if (has_messagetype()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteEnumToArray(
      1, this->messagetype(), target);
  }
  
  // required string topicName = 2;
  if (has_topicname()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->topicname().data(), this->topicname().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        2, this->topicname(), target);
  }
  
  // optional bytes messagePayload = 3;
  if (has_messagepayload()) {
    target =
      ::google::protobuf::internal::WireFormatLite::WriteBytesToArray(
        3, this->messagepayload(), target);
  }
  
  if (!unknown_fields().empty()) {
    target = ::google::protobuf::internal::WireFormat::SerializeUnknownFieldsToArray(
        unknown_fields(), target);
  }
  return target;
}

int BrokerToClientMessage::ByteSize() const {
  int total_size = 0;
  
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required .sms.protocol.protobuf.BrokerToClientMessage.BrokerToClientMessageType messageType = 1;
    if (has_messagetype()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::EnumSize(this->messagetype());
    }
    
    // required string topicName = 2;
    if (has_topicname()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->topicname());
    }
    
    // optional bytes messagePayload = 3;
    if (has_messagepayload()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::BytesSize(
          this->messagepayload());
    }
    
  }
  if (!unknown_fields().empty()) {
    total_size +=
      ::google::protobuf::internal::WireFormat::ComputeUnknownFieldsSize(
        unknown_fields());
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void BrokerToClientMessage::MergeFrom(const ::google::protobuf::Message& from) {
  GOOGLE_CHECK_NE(&from, this);
  const BrokerToClientMessage* source =
    ::google::protobuf::internal::dynamic_cast_if_available<const BrokerToClientMessage*>(
      &from);
  if (source == NULL) {
    ::google::protobuf::internal::ReflectionOps::Merge(from, this);
  } else {
    MergeFrom(*source);
  }
}

void BrokerToClientMessage::MergeFrom(const BrokerToClientMessage& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_messagetype()) {
      set_messagetype(from.messagetype());
    }
    if (from.has_topicname()) {
      set_topicname(from.topicname());
    }
    if (from.has_messagepayload()) {
      set_messagepayload(from.messagepayload());
    }
  }
  mutable_unknown_fields()->MergeFrom(from.unknown_fields());
}

void BrokerToClientMessage::CopyFrom(const ::google::protobuf::Message& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

void BrokerToClientMessage::CopyFrom(const BrokerToClientMessage& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool BrokerToClientMessage::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000003) != 0x00000003) return false;
  
  return true;
}

void BrokerToClientMessage::Swap(BrokerToClientMessage* other) {
  if (other != this) {
    std::swap(messagetype_, other->messagetype_);
    std::swap(topicname_, other->topicname_);
    std::swap(messagepayload_, other->messagepayload_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    _unknown_fields_.Swap(&other->_unknown_fields_);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::google::protobuf::Metadata BrokerToClientMessage::GetMetadata() const {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::Metadata metadata;
  metadata.descriptor = BrokerToClientMessage_descriptor_;
  metadata.reflection = BrokerToClientMessage_reflection_;
  return metadata;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace protobuf
}  // namespace protocol
}  // namespace sms

// @@protoc_insertion_point(global_scope)
