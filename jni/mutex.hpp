#ifndef net_wanderinghorse_WHNET_MUTEX_HPP_INCLUDED
#define net_wanderinghorse_WHNET_MUTEX_HPP_INCLUDED

namespace v8 { namespace juice {

	/**
	   Represents a mutex used for locking threads.

	   Notes about the underlying mutex implementation(s):

	   - On single-threaded builds this class has no underlying
	   native mutex and does nothing.

	   - On pthreads builds, the underlying native mutex is a
	   per-instance pthread mutex.

	   - On Win32, per-instance critical sections are used.

	   To simulate a global mutex, create a shared instance of
	   this type, e.g., via a myApp::getGlobalMutex() function.
	*/
	class mutex
	{
	public:
		/**
		   Initializes this mutex.
		*/
		mutex();
		/**
		   Closes the mutex.
		*/
		~mutex() throw();
		/**
		   Locks this mutex. Trying to lock it a second (or
		   subsequent) time will cause this function to wait
		   until the previous lock(s) is (are) released.

		   Recursive locks are not supported, which means that
		   if you call lock() twice on a mutex without an
		   intervening unlock(), you will deadlock.  Code
		   using this mutex must be careful to avoid this case
		   (see the mutex_sentry class, which is one
		   solution).

		   Returns a reference to this object mainly to allow this call
		   to be used in ctor member initialization lists, but maybe
		   that will have some other use eventually.
		*/
		mutex & lock();
		/**
		   Unlocks this mutex.
		*/
		void unlock();

		/**
		   Copying a mutex is a no-op. It does nothing but the
		   operators are supported to enable the
		   implementation of client classes which want
		   per-instance mutex members.
		*/
		mutex & operator=( mutex const & );
		/**
		   This ctor ignores its argument and behaves identically
		   to the default ctor. See operator=().
		*/
		mutex( mutex const & );
	private:
		struct impl; // opaque internal type
		impl * m_p;
	};

        /**
           This sentry class locks a mutex on construction and unlocks
           in on destruction. The intended usage is to instantiate it
           at the start of a routine which needs a lock. The
           instantiation will not return until the lock is acquired or
           the locking function throws an exception. Upon destruction
	   of this object, the mutex will be unlocked.

	   Note that these objects are not copyable.
        */
        class mutex_sentry
        {
        public:
		/** Calls mx.lock(). mx must outlive
		    this object.
		*/
                explicit mutex_sentry( mutex & mx );
		/** Unlocks the mutex we locked in the ctor. */
                ~mutex_sentry() throw();
	private:
		mutex_sentry & operator=( mutex_sentry const & ); // unimplemented!
		mutex_sentry( mutex_sentry const & ); // unimplemented!
		mutex & mx;
        };

}} // namespaces

#endif // net_wanderinghorse_WHNET_MUTEX_HPP_INCLUDED