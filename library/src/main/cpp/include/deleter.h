#ifndef STT_DELETER_H
#define STT_DELETER_H

#include "whisper.h"

struct whisper_context_deleter {
    void operator()(whisper_context *context) { whisper_free(context); }
};

typedef std::unique_ptr<whisper_context, whisper_context_deleter> whisper_context_ptr;

#endif //STT_DELETER_H