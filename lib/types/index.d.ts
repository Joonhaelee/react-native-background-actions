export default backgroundServer;

export type BackgroundTaskUpdateOptions = {
    taskName: string;
    taskTitle: string;
    taskDesc: string;
    taskIcon: {
        name: string;
        type: string;
        package?: string;
    };
    color?: string | undefined;
    linkingURI?: string | undefined;
    progressBar?:
        | {
              max: number;
              value: number;
              indeterminate?: boolean | undefined;
          }
        | undefined;
};

export type BackgroundTaskStartOptions = BackgroundTaskUpdateOptions & {
    autoCancel?: boolean;
    ongoing?: boolean;
    vibrate?: string;
};

declare const backgroundServer: BackgroundServer;
/**
 * @typedef {{taskName: string,
 *            taskTitle: string,
 *            taskDesc: string,
 *            taskIcon: {name: string, type: string, package?: string},
 *            color?: string
 *            linkingURI?: string,
 *            progressBar?: {max: number, value: number, indeterminate?: boolean}
 *            }} BackgroundTaskOptions
 * @extends EventEmitter<'expiration',any>
 */
declare class BackgroundServer extends EventEmitter<'expiration', any> {
    /** @private */
    private _runnedTasks;
    /** @private @type {(arg0?: any) => void} */
    private _stopTask;
    /** @private */
    private _isRunning;
    /** @private @type {BackgroundTaskOptions} */
    private _currentOptions;
    /**
     * @private
     */
    private _addListeners;
    /**
     * **ANDROID ONLY**
     * Updates the task notification.
     * *On iOS this method will return immediately*
     *
     * @param {taskData: BackgroundTaskUpdateOptions}}
     */

    updateNotification(taskData: BackgroundTaskUpdateOptions): Promise<void>;

    /**
     * Returns if the current background task is running.
     *
     * It returns `true` if `start()` has been called and the task has not finished.
     *
     * It returns `false` if `stop()` has been called, **even if the task has not finished**.
     */
    isRunning(): boolean;
    /**
     * @template T
     *
     * @param {(taskData?: T) => Promise<void>} task
     * @param {BackgroundTaskStartOptions & {parameters?: T}} options
     * @returns {Promise<void>}
     */
    start<T>(
        task: (taskData?: T) => Promise<void>,
        options: BackgroundTaskStartOptions & {
            parameters?: T;
        }
    ): Promise<void>;

    /**
     * @private
     * @template T
     * @param {(taskData?: T) => Promise<void>} task
     * @param {T} [parameters]
     */
    private _generateTask;
    /**
     * @private
     * @param {BackgroundTaskOptions} options
     */
    private _normalizeOptions;
    /**
     * Stops the background task.
     *
     * @returns {Promise<void>}
     */
    stop(): Promise<void>;
}
import EventEmitter from 'eventemitter3';
