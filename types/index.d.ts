 
// Copyright (c) Microsoft Open Technologies Inc
// Licensed under the MIT license.
interface Navigator {
	ocrimpl:Ocrimpl;
}

interface Ocrimpl {
    /**
     * This plugin provides an API for taking pictures and for choosing images from the system's image library.
     @param method represent s the method name which you want to call
     @param params represent the parameters
     @param onSuccess  send the success callback
     @param onFail send the failure callnback
     */
    request(
        method:string,
        params:string,
        onSuccess:(response: Response) => void,
        onFail: (error:any) => void
        ):void;
}

