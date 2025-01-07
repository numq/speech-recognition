#ifndef STT_DELETER_H
#define STT_DELETER_H

#include "whisper.h"

struct WhisperContextDeleter {
    void operator()(whisper_context *context) const {
        if (context) {
            whisper_free(context);
        }
    }
};

#endif //STT_DELETER_H