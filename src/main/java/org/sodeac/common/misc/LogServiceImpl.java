package org.sodeac.common.misc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.sodeac.common.ILogService;
import org.sodeac.common.model.CommonBaseBranchNodeType;
import org.sodeac.common.model.CoreTreeModel;
import org.sodeac.common.model.ThrowableNodeType;
import org.sodeac.common.model.logging.LogEventNodeType;
import org.sodeac.common.model.logging.LogEventType;
import org.sodeac.common.model.logging.LogLevel;
import org.sodeac.common.model.logging.LogPropertyNodeType;
import org.sodeac.common.model.logging.LogPropertyType;
import org.sodeac.common.model.logging.LoggingTreeModel;
import org.sodeac.common.typedtree.XMLMarshaller;
import org.sodeac.common.typedtree.BranchNode;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.TypedTreeMetaModel;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

public class LogServiceImpl implements ILogService 
{
	private volatile LogLevel writeLogLevel = LogLevel.INFO;
	private volatile String defaultDomain = null;
	private volatile String defaultSource = null;
	private volatile LogEventType defaultLogEventType = LogEventType.SYSTEM_LOG;
	private volatile List<Consumer<BranchNode<?,LogEventNodeType>>> backendList = new CopyOnWriteArrayList<Consumer<BranchNode<?,LogEventNodeType>>>();
	private volatile boolean autoDispose = true;
	
	private XMLMarshaller xmlMarshaller = null;
	
	public LogServiceImpl()
	{
		super();
		
		this.xmlMarshaller = ModelRegistry.getTypedTreeMetaModel(CoreTreeModel.class).getXMLMarshaller();
	}
	
	private AtomicLong sequencer = new AtomicLong();
	
	public LogServiceImpl addLoggerBackend(Consumer<BranchNode<?,LogEventNodeType>> logger)
	{
		if(! backendList.contains(logger))
		{
			backendList.add(logger);
		}
		return this;
	}
	
	public LogServiceImpl removeLoggerBackend(Consumer<BranchNode<?,LogEventNodeType>> logger)
	{
		while(backendList.contains(logger))
		{
			backendList.remove(logger);
		}
		return this;
	}
	
	@Override
	public LogLevel getWriteLogLevel() 
	{
		return this.writeLogLevel;
	}

	@Override
	public ILogService setWriteLogLevel(LogLevel logLevel) 
	{
		if(logLevel == null)
		{
			logLevel = LogLevel.INFO;
		}
		this.writeLogLevel = logLevel;
		return this;
	}

	@Override
	public ILogService setDefaultDomain(String domain) 
	{
		this.defaultDomain = domain;
		return this;
	}

	@Override
	public ILogService setDefaultSource(String source) 
	{
		this.defaultSource = source;
		return this;
	}
	
	@Override
	public ILogService setDefaultLogEventType(LogEventType logEventType)
	{
		if(logEventType ==  null)
		{
			logEventType = LogEventType.SYSTEM_LOG;
		}
		this.defaultLogEventType = logEventType;
		return this;
	}

	@Override
	public ILogService setAutoDispose(boolean autoDispose)
	{
		this.autoDispose  = autoDispose;
		return this;
	}
	
	@Override
	public ILogEventBuilder newEvent() 
	{
		return new LogEventBuilderImpl();
	}
	
	private class LogEventBuilderImpl implements ILogEventBuilder
	{
		private String domain = null; 
		private String source = null;
		private String format = null;
		private LogEventType logEventType = null;
		private LogLevel logLevel = LogLevel.INFO;
		private String messageString = null;
		private List<LogPropertyBuilder> properties = null;
		
		private LogEventBuilderImpl()
		{
			super();
			this.domain = LogServiceImpl.this.defaultDomain;
			this.source = LogServiceImpl.this.defaultSource;
			this.logEventType = LogServiceImpl.this.defaultLogEventType;
		}
		
		private List<LogPropertyBuilder> getProperties()
		{
			Objects.nonNull(this.logEventType);
			if(this.properties == null)
			{
				this.properties = new ArrayList<LogServiceImpl.LogEventBuilderImpl.LogPropertyBuilder>();
			}
			return this.properties;
		}

		@Override
		public ILogEventBuilder setLogItemLevel(LogLevel logLevel) 
		{
			Objects.nonNull(this.logEventType);
			if(logLevel == null)
			{
				logLevel = LogLevel.INFO;
			}
			this.logLevel = logLevel;
			return this;
		}

		@Override
		public ILogEventBuilder setLogEventType(LogEventType logEventType) 
		{
			Objects.nonNull(this.logEventType);
			if(logEventType == null)
			{
				logEventType = LogEventType.SYSTEM_LOG;
			}
			this.logEventType = logEventType;
			return this;
		}

		@Override
		public ILogEventBuilder setDomain(String domain) 
		{
			Objects.nonNull(this.logEventType);
			this.domain = domain;
			return this;
		}

		@Override
		public ILogEventBuilder setSource(String source) 
		{
			Objects.nonNull(this.logEventType);
			this.source = source;
			return this;
		}

		@Override
		public ILogEventBuilder setFormat(String format) 
		{
			Objects.nonNull(this.logEventType);
			this.format = format;
			return this ;
		}

		@Override
		public ILogEventBuilder setMessage(String message) 
		{
			Objects.nonNull(this.logEventType);
			this.messageString = message;
			return this;
		}

		@Override
		public ILogEventBuilder addProperty(String key, String value) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.PROPERTY;
			property.key = key;
			property.value = value;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addProperty(String key, String value, String format) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.PROPERTY;
			property.key = key;
			property.value = value;
			property.format = format;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addProperty(String key, String value, String format, String domain) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.PROPERTY;
			property.key = key;
			property.value = value;
			property.format = format;
			property.domain = domain;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addTag(String tag) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.TAG;
			property.key = tag;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addComment(String comment) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.COMMENT;
			property.value = comment;
			getProperties().add(property);
			return this;
		}
		
		@Override
		public ILogEventBuilder addComment(String comment, String id, String format)
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.COMMENT;
			property.key = id;
			property.value = comment;
			property.format = format;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addThrowable(Throwable throwable) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.THROWABLE;
			property.throwable = throwable;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogEventBuilder addStacktrace(StackTraceElement[] stacktrace) 
		{
			Objects.nonNull(this.logEventType);
			LogPropertyBuilder property = new LogPropertyBuilder();
			property.type = LogPropertyType.STACKTRACE;
			property.stacktrace = stacktrace;
			getProperties().add(property);
			return this;
		}

		@Override
		public ILogService fire() 
		{
			Objects.nonNull(this.logEventType);
			
			if(LogServiceImpl.this.getWriteLogLevel().getIntValue() > this.logLevel.getIntValue())
			{
				if(properties != null)
				{
					for(LogPropertyBuilder propertyBuilder : properties)
					{
						propertyBuilder.dispose();
					}
					properties.clear();
				}
				
				domain = null; 
				source = null;
				format = null;
				logEventType = null;
				logLevel = null;
				messageString = null;
				properties = null;
				
				return LogServiceImpl.this;
			}
			
			RootBranchNode<LoggingTreeModel,LogEventNodeType> logEvent = TypedTreeMetaModel.getInstance(LoggingTreeModel.class).createRootNode(LoggingTreeModel.logEvent);
			
			logEvent
				.setValue(LogEventNodeType.type, logEventType.name())
				.setValue(LogEventNodeType.logLevelName, logLevel.name())
				.setValue(LogEventNodeType.logLevelValue, logLevel.getIntValue())
				.setValue(LogEventNodeType.domain, domain)
				.setValue(LogEventNodeType.createClientURI, source)
				.setValue(LogEventNodeType.format, format)
				.setValue(LogEventNodeType.sequence, LogServiceImpl.this.sequencer.getAndIncrement())
				.setValue(LogEventNodeType.timestamp, new Date());
			
			logEvent.setValue(LogEventNodeType.message, this.messageString);
			if(properties != null)
			{
				for(LogPropertyBuilder propertyBuilder : properties)
				{
					BranchNode<LogEventNodeType, LogPropertyNodeType> property = logEvent.create(LogEventNodeType.propertyList)
						.setValue(LogPropertyNodeType.type, propertyBuilder.type.name())
						.setValue(LogPropertyNodeType.domain, propertyBuilder.domain)
						.setValue(LogPropertyNodeType.key, propertyBuilder.key)
						.setValue(LogPropertyNodeType.format, propertyBuilder.format)
						.setValue(LogPropertyNodeType.value, propertyBuilder.value);
					
					if(propertyBuilder.throwable != null)
					{
						RootBranchNode<CoreTreeModel,ThrowableNodeType> nodeFromThrowable = ThrowableNodeType.nodeFromThrowable(propertyBuilder.throwable);
						
						try
						{
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							xmlMarshaller.marshal(nodeFromThrowable, baos,true);
							property.setValue(LogPropertyNodeType.value,baos.toString());
							baos = null;
						}
						catch (Exception e) 
						{
							throw new RuntimeException(e);
						}
					}
					// TODO stacktrace
					
					propertyBuilder.dispose();
				}
				properties.clear();
			}
			
			domain = null; 
			source = null;
			format = null;
			logEventType = null;
			logLevel = null;
			messageString = null;
			properties = null;
			
			for(Consumer<BranchNode<?,LogEventNodeType>> backend : LogServiceImpl.this.backendList)
			{
				backend.accept(logEvent);
			}
			
			if(autoDispose)
			{
				logEvent.dispose(); 
			}
			return LogServiceImpl.this;
		}
		
		private class LogPropertyBuilder
		{
			private LogPropertyType type;
			private String key;
			private String value; 
			private String format;
			private String domain;
			private Throwable throwable;
			private StackTraceElement[] stacktrace;
			
			private void dispose()
			{
				this.type = null;
				this.key = null;
				this.value = null; 
				this.format = null;
				this.domain = null;
				this.throwable = null;
				this.stacktrace = null;
			}
		}
		
	}

}
