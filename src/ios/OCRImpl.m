#import "OCRImpl.h"
#import "Reachability.h"
#import "Client.h"

@interface OCRImpl () <ClientDelegate>{
    //MARK: - Class Properties
    NSString *ocrCallbackId;
    NSDictionary *requestDict;
    NSString *app_ID ;
    NSString *app_password ;
    NSString *imagePath;
}
@end

@implementation OCRImpl

- (void)startImageProcessing:(CDVInvokedUrlCommand*)command
{
   
    //[self.commandDelegate runInBackground:^{
        
        ocrCallbackId = [command callbackId];
        
        NSString* request = [[command arguments] objectAtIndex:0];
        NSLog(@"Request <<<<<<<<<< : %@",request);
        NSData *stringValueData = [request dataUsingEncoding:NSUTF8StringEncoding];
        
        NSError *jsonError = nil;
        id requests = [NSJSONSerialization JSONObjectWithData:stringValueData options:0 error:&jsonError];
        
        if([requests isKindOfClass:[NSDictionary class]])
        {
            requests = [NSArray arrayWithObject:requests];
            requestDict = [requests objectAtIndex:0];
            if([requestDict valueForKey:@"app_ID"])
                app_ID = [requestDict valueForKey:@"app_ID"];
            if([requestDict valueForKey:@"app_password"])
                app_password = [requestDict valueForKey:@"app_password"];
            if([requestDict valueForKey:@"imagePath"])
                imagePath = [requestDict valueForKey:@"imagePath"];
        }
        imagePath = [imagePath substringFromIndex:7];
        NSLog(@"image path %@", imagePath);
       
    //}];
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *path = [paths objectAtIndex:0];
    /*For Development we are sharing files through iTunes
     */
    NSString *imageDataPath = [path stringByAppendingPathComponent:@"1501741907266.jpg"];
    NSLog(@"image data path %@",imageDataPath);
    UIImage *image = [UIImage imageWithContentsOfFile:imagePath];
    
    Client *client = [[Client alloc] initWithApplicationID:app_ID password:app_password];
    [client setDelegate:self];
    
    ProcessingParams* params = [[ProcessingParams alloc] init];
    
    [client processImage:image withParams:params];
}

#pragma mark - ClientDelegate implementation

- (void)clientDidFinishUpload:(Client *)sender
{
    //upload complete
    NSLog(@"finish upload");
}

- (void)clientDidFinishProcessing:(Client *)sender
{
    //processing complete
    NSLog(@"finish processing");
}

- (void)client:(Client *)sender didFinishDownloadData:(NSData *)downloadedData
{
    //download complete
    //Do your next task here
    NSString *response = [[NSString alloc] initWithData:downloadedData encoding:NSUTF8StringEncoding];
    NSLog(@"response  %@",response);
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:response];
    result.keepCallback = [NSNumber numberWithInt:1];
    [self.commandDelegate sendPluginResult:result callbackId:ocrCallbackId];
}

- (void)client:(Client *)sender didFailedWithError:(NSError *)error
{
    NSLog(@"error ");
    //got an error while downloading details
}


@end
