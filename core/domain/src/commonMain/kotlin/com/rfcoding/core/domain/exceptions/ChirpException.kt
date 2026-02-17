package com.rfcoding.core.domain.exceptions

import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result

class ChirpException(val error: Result.Failure<DataError>): Exception()