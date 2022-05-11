// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: DownloadRequestResponse.proto

package proto;

public final class DownloadRequestResponse {
  private DownloadRequestResponse() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface DownloadRequestResponseDetailOrBuilder extends
      // @@protoc_insertion_point(interface_extends:tutorial.DownloadRequestResponseDetail)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string fileName = 1;</code>
     * @return The fileName.
     */
    java.lang.String getFileName();
    /**
     * <code>string fileName = 1;</code>
     * @return The bytes for fileName.
     */
    com.google.protobuf.ByteString
        getFileNameBytes();

    /**
     * <code>uint64 packetNumber = 2;</code>
     * @return The packetNumber.
     */
    long getPacketNumber();

    /**
     * <code>bool dataAvailable = 3;</code>
     * @return The dataAvailable.
     */
    boolean getDataAvailable();

    /**
     * <code>bytes packetData = 4;</code>
     * @return The packetData.
     */
    com.google.protobuf.ByteString getPacketData();
  }
  /**
   * Protobuf type {@code tutorial.DownloadRequestResponseDetail}
   */
  public static final class DownloadRequestResponseDetail extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:tutorial.DownloadRequestResponseDetail)
      DownloadRequestResponseDetailOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use DownloadRequestResponseDetail.newBuilder() to construct.
    private DownloadRequestResponseDetail(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private DownloadRequestResponseDetail() {
      fileName_ = "";
      packetData_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new DownloadRequestResponseDetail();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private DownloadRequestResponseDetail(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              java.lang.String s = input.readStringRequireUtf8();

              fileName_ = s;
              break;
            }
            case 16: {

              packetNumber_ = input.readUInt64();
              break;
            }
            case 24: {

              dataAvailable_ = input.readBool();
              break;
            }
            case 34: {

              packetData_ = input.readBytes();
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto.DownloadRequestResponse.internal_static_tutorial_DownloadRequestResponseDetail_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto.DownloadRequestResponse.internal_static_tutorial_DownloadRequestResponseDetail_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto.DownloadRequestResponse.DownloadRequestResponseDetail.class, proto.DownloadRequestResponse.DownloadRequestResponseDetail.Builder.class);
    }

    public static final int FILENAME_FIELD_NUMBER = 1;
    private volatile java.lang.Object fileName_;
    /**
     * <code>string fileName = 1;</code>
     * @return The fileName.
     */
    @java.lang.Override
    public java.lang.String getFileName() {
      java.lang.Object ref = fileName_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        fileName_ = s;
        return s;
      }
    }
    /**
     * <code>string fileName = 1;</code>
     * @return The bytes for fileName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getFileNameBytes() {
      java.lang.Object ref = fileName_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        fileName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int PACKETNUMBER_FIELD_NUMBER = 2;
    private long packetNumber_;
    /**
     * <code>uint64 packetNumber = 2;</code>
     * @return The packetNumber.
     */
    @java.lang.Override
    public long getPacketNumber() {
      return packetNumber_;
    }

    public static final int DATAAVAILABLE_FIELD_NUMBER = 3;
    private boolean dataAvailable_;
    /**
     * <code>bool dataAvailable = 3;</code>
     * @return The dataAvailable.
     */
    @java.lang.Override
    public boolean getDataAvailable() {
      return dataAvailable_;
    }

    public static final int PACKETDATA_FIELD_NUMBER = 4;
    private com.google.protobuf.ByteString packetData_;
    /**
     * <code>bytes packetData = 4;</code>
     * @return The packetData.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getPacketData() {
      return packetData_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(fileName_)) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, fileName_);
      }
      if (packetNumber_ != 0L) {
        output.writeUInt64(2, packetNumber_);
      }
      if (dataAvailable_ != false) {
        output.writeBool(3, dataAvailable_);
      }
      if (!packetData_.isEmpty()) {
        output.writeBytes(4, packetData_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(fileName_)) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, fileName_);
      }
      if (packetNumber_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(2, packetNumber_);
      }
      if (dataAvailable_ != false) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(3, dataAvailable_);
      }
      if (!packetData_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(4, packetData_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof proto.DownloadRequestResponse.DownloadRequestResponseDetail)) {
        return super.equals(obj);
      }
      proto.DownloadRequestResponse.DownloadRequestResponseDetail other = (proto.DownloadRequestResponse.DownloadRequestResponseDetail) obj;

      if (!getFileName()
          .equals(other.getFileName())) return false;
      if (getPacketNumber()
          != other.getPacketNumber()) return false;
      if (getDataAvailable()
          != other.getDataAvailable()) return false;
      if (!getPacketData()
          .equals(other.getPacketData())) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + FILENAME_FIELD_NUMBER;
      hash = (53 * hash) + getFileName().hashCode();
      hash = (37 * hash) + PACKETNUMBER_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getPacketNumber());
      hash = (37 * hash) + DATAAVAILABLE_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getDataAvailable());
      hash = (37 * hash) + PACKETDATA_FIELD_NUMBER;
      hash = (53 * hash) + getPacketData().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(proto.DownloadRequestResponse.DownloadRequestResponseDetail prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code tutorial.DownloadRequestResponseDetail}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:tutorial.DownloadRequestResponseDetail)
        proto.DownloadRequestResponse.DownloadRequestResponseDetailOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return proto.DownloadRequestResponse.internal_static_tutorial_DownloadRequestResponseDetail_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return proto.DownloadRequestResponse.internal_static_tutorial_DownloadRequestResponseDetail_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                proto.DownloadRequestResponse.DownloadRequestResponseDetail.class, proto.DownloadRequestResponse.DownloadRequestResponseDetail.Builder.class);
      }

      // Construct using proto.DownloadRequestResponse.DownloadRequestResponseDetail.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        fileName_ = "";

        packetNumber_ = 0L;

        dataAvailable_ = false;

        packetData_ = com.google.protobuf.ByteString.EMPTY;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return proto.DownloadRequestResponse.internal_static_tutorial_DownloadRequestResponseDetail_descriptor;
      }

      @java.lang.Override
      public proto.DownloadRequestResponse.DownloadRequestResponseDetail getDefaultInstanceForType() {
        return proto.DownloadRequestResponse.DownloadRequestResponseDetail.getDefaultInstance();
      }

      @java.lang.Override
      public proto.DownloadRequestResponse.DownloadRequestResponseDetail build() {
        proto.DownloadRequestResponse.DownloadRequestResponseDetail result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public proto.DownloadRequestResponse.DownloadRequestResponseDetail buildPartial() {
        proto.DownloadRequestResponse.DownloadRequestResponseDetail result = new proto.DownloadRequestResponse.DownloadRequestResponseDetail(this);
        result.fileName_ = fileName_;
        result.packetNumber_ = packetNumber_;
        result.dataAvailable_ = dataAvailable_;
        result.packetData_ = packetData_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof proto.DownloadRequestResponse.DownloadRequestResponseDetail) {
          return mergeFrom((proto.DownloadRequestResponse.DownloadRequestResponseDetail)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(proto.DownloadRequestResponse.DownloadRequestResponseDetail other) {
        if (other == proto.DownloadRequestResponse.DownloadRequestResponseDetail.getDefaultInstance()) return this;
        if (!other.getFileName().isEmpty()) {
          fileName_ = other.fileName_;
          onChanged();
        }
        if (other.getPacketNumber() != 0L) {
          setPacketNumber(other.getPacketNumber());
        }
        if (other.getDataAvailable() != false) {
          setDataAvailable(other.getDataAvailable());
        }
        if (other.getPacketData() != com.google.protobuf.ByteString.EMPTY) {
          setPacketData(other.getPacketData());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        proto.DownloadRequestResponse.DownloadRequestResponseDetail parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (proto.DownloadRequestResponse.DownloadRequestResponseDetail) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private java.lang.Object fileName_ = "";
      /**
       * <code>string fileName = 1;</code>
       * @return The fileName.
       */
      public java.lang.String getFileName() {
        java.lang.Object ref = fileName_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          fileName_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string fileName = 1;</code>
       * @return The bytes for fileName.
       */
      public com.google.protobuf.ByteString
          getFileNameBytes() {
        java.lang.Object ref = fileName_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          fileName_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string fileName = 1;</code>
       * @param value The fileName to set.
       * @return This builder for chaining.
       */
      public Builder setFileName(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        fileName_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string fileName = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearFileName() {
        
        fileName_ = getDefaultInstance().getFileName();
        onChanged();
        return this;
      }
      /**
       * <code>string fileName = 1;</code>
       * @param value The bytes for fileName to set.
       * @return This builder for chaining.
       */
      public Builder setFileNameBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        fileName_ = value;
        onChanged();
        return this;
      }

      private long packetNumber_ ;
      /**
       * <code>uint64 packetNumber = 2;</code>
       * @return The packetNumber.
       */
      @java.lang.Override
      public long getPacketNumber() {
        return packetNumber_;
      }
      /**
       * <code>uint64 packetNumber = 2;</code>
       * @param value The packetNumber to set.
       * @return This builder for chaining.
       */
      public Builder setPacketNumber(long value) {
        
        packetNumber_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint64 packetNumber = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearPacketNumber() {
        
        packetNumber_ = 0L;
        onChanged();
        return this;
      }

      private boolean dataAvailable_ ;
      /**
       * <code>bool dataAvailable = 3;</code>
       * @return The dataAvailable.
       */
      @java.lang.Override
      public boolean getDataAvailable() {
        return dataAvailable_;
      }
      /**
       * <code>bool dataAvailable = 3;</code>
       * @param value The dataAvailable to set.
       * @return This builder for chaining.
       */
      public Builder setDataAvailable(boolean value) {
        
        dataAvailable_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bool dataAvailable = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearDataAvailable() {
        
        dataAvailable_ = false;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString packetData_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes packetData = 4;</code>
       * @return The packetData.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString getPacketData() {
        return packetData_;
      }
      /**
       * <code>bytes packetData = 4;</code>
       * @param value The packetData to set.
       * @return This builder for chaining.
       */
      public Builder setPacketData(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        packetData_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes packetData = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearPacketData() {
        
        packetData_ = getDefaultInstance().getPacketData();
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:tutorial.DownloadRequestResponseDetail)
    }

    // @@protoc_insertion_point(class_scope:tutorial.DownloadRequestResponseDetail)
    private static final proto.DownloadRequestResponse.DownloadRequestResponseDetail DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new proto.DownloadRequestResponse.DownloadRequestResponseDetail();
    }

    public static proto.DownloadRequestResponse.DownloadRequestResponseDetail getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<DownloadRequestResponseDetail>
        PARSER = new com.google.protobuf.AbstractParser<DownloadRequestResponseDetail>() {
      @java.lang.Override
      public DownloadRequestResponseDetail parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new DownloadRequestResponseDetail(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<DownloadRequestResponseDetail> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<DownloadRequestResponseDetail> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public proto.DownloadRequestResponse.DownloadRequestResponseDetail getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tutorial_DownloadRequestResponseDetail_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tutorial_DownloadRequestResponseDetail_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035DownloadRequestResponse.proto\022\010tutoria" +
      "l\"r\n\035DownloadRequestResponseDetail\022\020\n\010fi" +
      "leName\030\001 \001(\t\022\024\n\014packetNumber\030\002 \001(\004\022\025\n\rda" +
      "taAvailable\030\003 \001(\010\022\022\n\npacketData\030\004 \001(\014B \n" +
      "\005protoB\027DownloadRequestResponseb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_tutorial_DownloadRequestResponseDetail_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_tutorial_DownloadRequestResponseDetail_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tutorial_DownloadRequestResponseDetail_descriptor,
        new java.lang.String[] { "FileName", "PacketNumber", "DataAvailable", "PacketData", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
