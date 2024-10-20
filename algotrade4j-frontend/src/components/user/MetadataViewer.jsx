import React, {useState} from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "../ui/dialog.jsx";
import {Button} from "../ui/button.jsx";
import {Check, Copy} from "lucide-react";
import {useIsMobile} from '@/hooks/useIsMobile.js';

const MetadataViewer = ({metadata, title = 'Metadata'}) => {
    const [isCopied, setIsCopied] = useState(false);
    const isMobile = useIsMobile();

    const prettyJson = JSON.stringify(metadata, null, 2);

    const copyToClipboard = () => {
        navigator.clipboard.writeText(prettyJson);
        setIsCopied(true);
        setTimeout(() => setIsCopied(false), 2000);
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                    View Metadata
                </Button>
            </DialogTrigger>
            <DialogContent
                className={`${isMobile ? 'max-w-full w-full' : 'sm:max-w-[625px]'} `}
                style={{maxWidth: isMobile ? '100%' : '625px'}}
            >
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                </DialogHeader>
                <div className="bg-muted p-4 rounded-md relative">
                    <pre
                        className="text-sm overflow-auto max-h-[400px]"
                        style={{whiteSpace: 'pre-wrap', wordWrap: 'break-word'}}
                    >
                        {prettyJson}
                    </pre>
                    <Button className="absolute top-2 right-2" size="sm" onClick={copyToClipboard}>
                        {isCopied ? <Check className="h-4 w-4"/> : <Copy className="h-4 w-4"/>}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default MetadataViewer;